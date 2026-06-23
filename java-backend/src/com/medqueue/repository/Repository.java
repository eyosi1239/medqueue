package com.medqueue.repository;

import java.util.*;

public class Repository<T> {
    protected final Map<String, T> store = new LinkedHashMap<>();

    public void save(String id, T entity)          { store.put(id, entity); }
    public Optional<T> findById(String id)         { return Optional.ofNullable(store.get(id)); }
    public boolean existsById(String id)           { return store.containsKey(id); }
    public void deleteById(String id)              { store.remove(id); }
    public List<T> findAll()                       { return new ArrayList<>(store.values()); }
    public long count()                            { return store.size(); }
}
