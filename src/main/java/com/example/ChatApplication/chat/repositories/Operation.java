package com.example.ChatApplication.chat.repositories;

import com.example.ChatApplication.chat.models.ChatMessage;
import jakarta.persistence.criteria.*;

import java.io.UncheckedIOException;
import java.time.LocalDateTime;

public enum Operation {
    LIKE {
        @Override
        @SuppressWarnings("unchecked")
        public Predicate buildPredicate(CriteriaBuilder cb, Root<ChatMessage> root, SearchFilters filter) {
            return cb.like(cb.upper((Path<String>) filter.getField().resolvePath(root)), "%" + filter.getField().parse(filter.getValue().toUpperCase()) + "%");
        }
    },
    EQ{
        @Override
        public Predicate buildPredicate(CriteriaBuilder cb, Root<ChatMessage> root, SearchFilters filter) {
            if(!Comparable.class.isAssignableFrom(filter.getField().type))
                throw new IllegalArgumentException(
                        filter.getField().type + " is not comparable"
                );

            return cb.equal(filter.getField().resolvePath(root), filter.getField().parse(filter.getValue()));
        }
    },
    DURING{
        @Override
        @SuppressWarnings("unchecked")
        public Predicate buildPredicate(CriteriaBuilder cb, Root<ChatMessage> root, SearchFilters filter) {
            if(!Comparable.class.isAssignableFrom(filter.getField().type))
                throw new IllegalArgumentException(
                        filter.getField().type + " is not comparable"
                );
            LocalDateTime start = (LocalDateTime) filter.getField().parse(filter.getValue());
            LocalDateTime end = start.plusDays(1);
            return cb.between((Path<LocalDateTime>) filter.getField().resolvePath(root), start, end);


        }
    },
    GT{
        @Override
        @SuppressWarnings({"unchecked","rawtypes"})
        public  Predicate buildPredicate(CriteriaBuilder cb, Root<ChatMessage> root, SearchFilters filter) {
            if(!Comparable.class.isAssignableFrom(filter.getField().type))
                throw new IllegalArgumentException(filter.getField().type+" is no comparable");

            var path= (Path<? extends Comparable<?>>) filter.getField().resolvePath(root);

            return cb.greaterThan((Expression<? extends Comparable>)path,(Comparable) filter.getField().parse(filter.getValue()));
        }
    },
    LT{
        @Override
        @SuppressWarnings({"unchecked","rawtypes"})
        public  Predicate buildPredicate(CriteriaBuilder cb, Root<ChatMessage> root, SearchFilters filter) {
            if(!Comparable.class.isAssignableFrom(filter.getField().type))
                throw new IllegalArgumentException(filter.getField().type+" is no comparable");

            var path= (Path<? extends Comparable<?>>) filter.getField().resolvePath(root);
            return cb.lessThan((Expression<? extends Comparable>)path,(Comparable) filter.getField().parse(filter.getValue()));
        }
    };


    public abstract Predicate buildPredicate(CriteriaBuilder cb, Root<ChatMessage> root, SearchFilters filter);


}