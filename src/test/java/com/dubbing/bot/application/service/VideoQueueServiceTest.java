package com.dubbing.bot.application.service;

import com.dubbing.bot.application.dto.DubbingJob;
import com.dubbing.bot.application.port.JobPublisher;
import com.dubbing.bot.domain.model.User;
import com.dubbing.bot.domain.model.Video;
import com.dubbing.bot.domain.model.enums.UserStatus;
import com.dubbing.bot.domain.model.enums.VideoStatus;
import com.dubbing.bot.domain.repository.VideoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoQueueServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private JobPublisher jobPublisher;

    @InjectMocks
    private VideoQueueService service;

    private User activeUser() {
        return User.builder().id(7L).telegramId(100L).balanceMinutes(5)
                .status(UserStatus.ACTIVE).build();
    }

    @Test
    void enqueuePersistsPendingVideoAndPublishesJob() {
        when(userService.getOrCreate(100L)).thenReturn(activeUser());
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> {
            Video v = inv.getArgument(0);
            v.setId(42L);
            return v;
        });

        Video result = service.enqueue(100L, 555L, "https://youtu.be/abc");

        assertThat(result.getStatus()).isEqualTo(VideoStatus.PENDING);

        // Outside an active transaction the job is published immediately.
        ArgumentCaptor<DubbingJob> captor = ArgumentCaptor.forClass(DubbingJob.class);
        verify(jobPublisher).publish(captor.capture());
        DubbingJob job = captor.getValue();
        assertThat(job.videoId()).isEqualTo(42L);
        assertThat(job.userId()).isEqualTo(7L);
        assertThat(job.chatId()).isEqualTo(555L);
        assertThat(job.youtubeUrl()).isEqualTo("https://youtu.be/abc");
    }

    @Test
    void enqueueRejectsBlockedUser() {
        User blocked = activeUser();
        blocked.setStatus(UserStatus.BLOCKED);
        when(userService.getOrCreate(100L)).thenReturn(blocked);

        assertThatThrownBy(() -> service.enqueue(100L, 555L, "https://youtu.be/abc"))
                .isInstanceOf(IllegalStateException.class);
        verify(jobPublisher, never()).publish(any());
    }
}
