package io.katharsis.resource.mock.repository;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.User;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserRepository implements ResourceRepository<User, Long> {

    private static final RequestParams REQUEST_PARAMS = new RequestParams(null);

    // Used ThreadLocal in case of switching to TestNG and using concurrent tests
    private static final ThreadLocal<Map<Long, User>> THREAD_LOCAL_REPOSITORY = new ThreadLocal<Map<Long, User>>() {
        @Override
        protected Map<Long, User> initialValue() {
            return new HashMap<>();
        }
    };

    private static final UserToProjectRepository USER_TO_PROJECT_REPOSITORY = new UserToProjectRepository();

    @Override
    public <S extends User> S save(S entity) {
        entity.setId((long) (THREAD_LOCAL_REPOSITORY.get().size() + 1));
        THREAD_LOCAL_REPOSITORY.get().put(entity.getId(), entity);

        return entity;
    }

    @Override
    public User findOne(Long aLong, RequestParams requestParams) {
        User user = THREAD_LOCAL_REPOSITORY.get().get(aLong);
        if (user == null) {
            throw new ResourceNotFoundException(User.class.getCanonicalName());
        }
        Iterable<Project> assignedProjects = USER_TO_PROJECT_REPOSITORY.findManyTargets(aLong, "assignedProjects", REQUEST_PARAMS);
        user.setAssignedProjects(makeCollection(assignedProjects));

        return user;
    }

    @Override
    public Iterable<User> findAll(RequestParams requestParams) {
        return THREAD_LOCAL_REPOSITORY.get().values();
    }


    @Override
    public Iterable<User> findAll(Iterable<Long> longs, RequestParams requestParams) {
        return null;
    }

    @Override
    public void delete(Long aLong) {
        THREAD_LOCAL_REPOSITORY.get().remove(aLong);
    }

    private static <E> List<E> makeCollection(Iterable<E> iter) {
        List<E> list = new LinkedList<>();
        for (E item : iter) {
            list.add(item);
        }
        return list;
    }
}
