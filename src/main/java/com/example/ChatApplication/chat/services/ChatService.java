package com.example.ChatApplication.chat.services;

import com.example.ChatApplication.Exception.ChannelNotFoundException;
import com.example.ChatApplication.Exception.IsSameUserException;
import com.example.ChatApplication.chat.Dtos.ChatChannelInitializationDto;
import com.example.ChatApplication.chat.Dtos.ChatMessageDto;
import com.example.ChatApplication.chat.mappers.ChatMessageMapper;
import com.example.ChatApplication.chat.models.ChatChannel;
import com.example.ChatApplication.chat.models.ChatMessage;
import com.example.ChatApplication.chat.repositories.ChatChannelRepository;
import com.example.ChatApplication.chat.repositories.ChatMessageRepository;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.dtos.NotificationDto;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.ChatApplication.user.services.UserService;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ChatChannelRepository chatchannelRespository;

    public void SubmitChatMessage(ChatMessageDto chatMessageDto)
    throws BeansException,UsernameNotFoundException {
        ChatMessage chatMessage=ChatMessageMapper.mapMessageDtoToMessage(chatMessageDto);
        chatMessageRepository.save(chatMessage);
        User fromUser = userService.getUser(String.valueOf(chatMessage.getWriter().getId()));
        User recipientUser = userService.getUser(String.valueOf(chatMessage.getReciever().getId()));
        userService.notifyUser(recipientUser,new NotificationDto(
                            NotificationType.CHAT_MESSAGE.toString(),
                             fromUser.getUsername()+" has sent you message" ,
                              chatMessage.getWriter().getId()
                              )
                                );
    }

    public Long establishChatSession(ChatChannelInitializationDto chatChannelInitializationDto)
        throws IsSameUserException,UsernameNotFoundException ,BeansException {

        Long channelId=getExistingChannel(chatChannelInitializationDto);
        return channelId!=null ? channelId : createNewChatChannel(chatChannelInitializationDto);
    }

    private Long createNewChatChannel(ChatChannelInitializationDto chatChannelInitializationDto)
            throws BeansException, UsernameNotFoundException{
        ChatChannel channel= new ChatChannel(userService.getUser(chatChannelInitializationDto.getUserOneId())
                                                ,userService.getUser(chatChannelInitializationDto.getUserTwoId()));

        chatchannelRespository.save(channel);
        return channel.getId();
    }

    private Long getExistingChannel(ChatChannelInitializationDto chatChannelInitializationDto) {
        List<ChatChannel> channel=chatchannelRespository.findExistingChannel(chatChannelInitializationDto.getUserOneId()
                                    ,chatChannelInitializationDto.getUserTwoId());
        return (channel!=null && !channel.isEmpty()) ? channel.get(0).getId() : null;
    }

    public List<ChatMessageDto> getChatMessages(Long channelId) {
        ChatChannel channel=chatchannelRespository.getChannelDetail(channelId);
        if(channel==null)
            throw new ChannelNotFoundException();

        List<ChatMessage> chatMessages= chatMessageRepository.getExistingChatMessages(
                                                                channel.getUserOne().getId(),
                                                                     channel.getUserTwo().getId(),
                                                                          PageRequest.of(0,20));
        List<ChatMessage> messagesByLatest= chatMessages.reversed();
        return ChatMessageMapper.mapMessagesToChatDtos(messagesByLatest);
    }
}
