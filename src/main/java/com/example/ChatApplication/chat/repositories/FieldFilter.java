package com.example.ChatApplication.chat.repositories;

import com.example.ChatApplication.chat.models.ChatMessage_;
import com.example.ChatApplication.user.User_;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;

public enum FieldFilter {

    CONTENT("content",String.class){

        @Override
        public Path<String> resolvePath(Root<?> root) {
            return root.get(CONTENT.fieldName);
        }
        @Override
        public String parse(String value){
            return  value;
        }
    },

    WRITER_ID("writer.id",Long.class){
        @Override
        public Path<Long> resolvePath(Root<?> root) {
               return root.get(ChatMessage_.WRITER).get(User_.ID);
        }
        @Override
        public Long parse(String value) {
            return Long.valueOf(value);
        }
    },

     TIME_SENT("timeSent",LocalDateTime.class){
         @Override
         public Path<LocalDate> resolvePath(Root<?> root) {
             return root.get(ChatMessage_.TIME_SENT);
         }

         @Override
         public LocalDateTime parse(String value) {
            try{
                return LocalDate.parse(value).atStartOfDay();
            }catch (DateTimeParseException e){
                throw new IllegalArgumentException("Date must be in the format yyyy-MM-dd. Received: " + value, e);
            }
         }
     };

     public final String fieldName;
     public final Class<?> type;

    FieldFilter(String fieldName,Class<?> type) {
        this.fieldName = fieldName;
        this.type=type;
    }

    public abstract Path<?> resolvePath(Root<?> root);
    public abstract Object parse(String value);


}
