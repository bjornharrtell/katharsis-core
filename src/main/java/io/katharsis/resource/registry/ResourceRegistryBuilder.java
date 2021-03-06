package io.katharsis.resource.registry;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.repository.NotFoundRepository;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.exception.RepositoryInstanceNotFoundException;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import net.jodah.typetools.TypeResolver;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Builder responsible for building an instance of ResourceRegistry.
 */
public class ResourceRegistryBuilder {

    private final JsonServiceLocator context;
    private final ResourceInformationBuilder resourceInformationBuilder;
    private final Logger logger = LoggerFactory.getLogger(ResourceRegistryBuilder.class);

    public ResourceRegistryBuilder(JsonServiceLocator context, ResourceInformationBuilder resourceInformationBuilder) {
        this.context = context;
        this.resourceInformationBuilder = resourceInformationBuilder;
    }

    /**
     * Scans all classes in provided package and finds all resources and repositories associated with found resource.
     *
     * @param packageName Package containing resources (models) and repositories.
     * @param serviceUrl  URL to the service
     * @return an instance of ResourceRegistry
     */
    public ResourceRegistry build(String packageName, @SuppressWarnings("SameParameterValue") String serviceUrl) {
        Reflections reflections;
        if (packageName != null) {
            String[] packageNames = packageName.split(",");
            reflections = new Reflections(packageNames);
        } else {
            reflections = new Reflections(packageName);
        }


        Set<Class<?>> jsonApiResources = reflections.getTypesAnnotatedWith(JsonApiResource.class);
        Set<Class<? extends ResourceRepository>> entityRepositoryClasses = reflections.getSubTypesOf(ResourceRepository.class);
        Set<Class<? extends RelationshipRepository>> relationshipRepositoryClasses = reflections
            .getSubTypesOf(RelationshipRepository.class);

        ResourceRegistry resourceRegistry = new ResourceRegistry(serviceUrl);
        for (Class resourceClass : jsonApiResources) {
            Class<? extends ResourceRepository> foundEntityRepositoryClass = findEntityRepository(resourceClass,
                entityRepositoryClasses);
            Set<Class<? extends RelationshipRepository>> foundRelationshipRepositoriesClasses =
                findRelationshipRepositories(resourceClass, relationshipRepositoryClasses);

            RegistryEntry registryEntry;
            if (foundEntityRepositoryClass == null) {
                registryEntry = createNotFoundEntry(resourceClass, foundRelationshipRepositoriesClasses);
            } else {
                registryEntry = createEntry(resourceClass, foundEntityRepositoryClass,
                    foundRelationshipRepositoriesClasses);
            }

            resourceRegistry.addEntry(resourceClass, registryEntry);
        }

        return resourceRegistry;
    }

    private RegistryEntry createNotFoundEntry(Class resourceClass,
                                              Set<Class<? extends RelationshipRepository>> foundRelationshipRepositoriesClasses) {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(resourceClass);
        ResourceRepository resourceRepository = new NotFoundRepository(resourceClass);
        List<RelationshipRepository> relationshipRepositories = initializeRelationshipRepositories(
            foundRelationshipRepositoriesClasses, resourceClass);
        //noinspection unchecked
        return new RegistryEntry(resourceInformation, resourceRepository, relationshipRepositories);
    }

    private Class<? extends ResourceRepository> findEntityRepository(Class resourceClass,
                                                                     Set<Class<? extends ResourceRepository>> entityRepositoryClasses) {
        for (Class<? extends ResourceRepository> entityRepositoryClass : entityRepositoryClasses) {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepository.class, entityRepositoryClass);
            if (typeArgs[0] == resourceClass) {
                return entityRepositoryClass;
            }
        }

        return null;
    }

    private Set<Class<? extends RelationshipRepository>> findRelationshipRepositories(Class resourceClass,
                                                                                      Set<Class<? extends RelationshipRepository>> relationshipRepositoryClasses) {
        Set<Class<? extends RelationshipRepository>> foundRelationshipRepositories = new HashSet<>();
        for (Class<? extends RelationshipRepository> relationshipRepository : relationshipRepositoryClasses) {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, relationshipRepository);
            if (typeArgs[0] == resourceClass) {
                foundRelationshipRepositories.add(relationshipRepository);
            }
        }
        return foundRelationshipRepositories;
    }

    private RegistryEntry createEntry(Class resourceClass, Class<? extends ResourceRepository> foundEntityRepositoryClass,
                                      Set<Class<? extends RelationshipRepository>> foundRelationshipRepositoriesClasses) {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(resourceClass);

        ResourceRepository resourceRepository = context.getInstance(foundEntityRepositoryClass);
        if (resourceRepository == null) {
            throw new RepositoryInstanceNotFoundException(foundEntityRepositoryClass.getCanonicalName());
        }

        logger.debug("Assigned {} ResourceRepository to {} resource class",
            foundEntityRepositoryClass.getCanonicalName(), resourceClass.getCanonicalName());

        List<RelationshipRepository> relationshipRepositories =
            initializeRelationshipRepositories(foundRelationshipRepositoriesClasses, resourceClass);
        //noinspection unchecked
        return new RegistryEntry(resourceInformation, resourceRepository, relationshipRepositories);
    }

    private List<RelationshipRepository> initializeRelationshipRepositories(
        Set<Class<? extends RelationshipRepository>> foundRelationshipRepositoriesClasses, Class resourceClass) {
        List<RelationshipRepository> relationshipRepositories = new LinkedList<>();
        for (Class<? extends RelationshipRepository> relationshipRepositoryClass : foundRelationshipRepositoriesClasses) {
            RelationshipRepository relationshipRepository = context.getInstance(relationshipRepositoryClass);
            if (relationshipRepository == null) {
                throw new RepositoryInstanceNotFoundException(relationshipRepositoryClass.getCanonicalName());
            }

            logger.debug("Assigned {} RelationshipRepository  to {} resource class",
                relationshipRepositoryClass.getCanonicalName(), resourceClass.getCanonicalName());

            relationshipRepositories.add(relationshipRepository);
        }
        return relationshipRepositories;
    }
}
