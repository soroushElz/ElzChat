package com.example.ChatApplication.chat.repositories;

import com.example.ChatApplication.chat.models.ForwardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForwardInfoRepository extends JpaRepository<ForwardInfo,Long> {


}

