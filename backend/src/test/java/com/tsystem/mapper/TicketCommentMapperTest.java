package com.tsystem.mapper;

import com.tsystem.model.TicketComment;
import com.tsystem.model.dto.response.TicketCommentResponse;
import com.tsystem.model.mapper.TicketCommentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TicketCommentMapperTest {

    private TicketComment testComment;
    private UUID commentId;
    private UUID ticketId;
    private UUID authorId;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        now = OffsetDateTime.now();

        testComment = TicketComment.builder()
                .id(commentId)
                .ticketId(ticketId)
                .authorId(authorId)
                .text("Test comment text")
                .createdAt(now)
                .build();
    }

    @Nested
    @DisplayName("toResponse Tests")
    class ToResponseTests {

        @Test
        @DisplayName("converts all fields correctly")
        void toResponse_ConvertsAllFields() {
            TicketCommentResponse response = TicketCommentMapper.toResponse(testComment);

            assertNotNull(response);
            assertEquals(commentId, response.getId());
            assertEquals(ticketId, response.getTicketId());
            assertEquals(authorId, response.getCommentAuthorId());
            assertEquals("Test comment text", response.getText());
        }

        @Test
        @DisplayName("handles empty text")
        void toResponse_EmptyText() {
            testComment.setText("");

            TicketCommentResponse response = TicketCommentMapper.toResponse(testComment);

            assertEquals("", response.getText());
        }

        @Test
        @DisplayName("handles null text")
        void toResponse_NullText() {
            testComment.setText(null);

            TicketCommentResponse response = TicketCommentMapper.toResponse(testComment);

            assertNull(response.getText());
        }

        @Test
        @DisplayName("handles long text")
        void toResponse_LongText() {
            String longText = "A".repeat(10000);
            testComment.setText(longText);

            TicketCommentResponse response = TicketCommentMapper.toResponse(testComment);

            assertEquals(longText, response.getText());
            assertEquals(10000, response.getText().length());
        }

        @Test
        @DisplayName("handles text with special characters")
        void toResponse_SpecialCharacters() {
            testComment.setText("Test with special chars: <>&\"'@#$%^*()");

            TicketCommentResponse response = TicketCommentMapper.toResponse(testComment);

            assertEquals("Test with special chars: <>&\"'@#$%^*()", response.getText());
        }

        @Test
        @DisplayName("handles multiline text")
        void toResponse_MultilineText() {
            testComment.setText("Line 1\nLine 2\nLine 3");

            TicketCommentResponse response = TicketCommentMapper.toResponse(testComment);

            assertEquals("Line 1\nLine 2\nLine 3", response.getText());
        }
    }

    @Nested
    @DisplayName("toResponseList Tests")
    class ToResponseListTests {

        @Test
        @DisplayName("converts list of comments")
        void toResponseList_ConvertsList() {
            TicketComment comment2 = TicketComment.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .text("Second comment")
                    .build();

            List<TicketCommentResponse> responses = TicketCommentMapper.toResponseList(
                    List.of(testComment, comment2)
            );

            assertEquals(2, responses.size());
            assertEquals("Test comment text", responses.get(0).getText());
            assertEquals("Second comment", responses.get(1).getText());
        }

        @Test
        @DisplayName("empty list returns empty list")
        void toResponseList_EmptyList() {
            List<TicketCommentResponse> responses = TicketCommentMapper.toResponseList(Collections.emptyList());

            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }

        @Test
        @DisplayName("single element list")
        void toResponseList_SingleElement() {
            List<TicketCommentResponse> responses = TicketCommentMapper.toResponseList(List.of(testComment));

            assertEquals(1, responses.size());
            assertEquals(commentId, responses.get(0).getId());
        }

        @Test
        @DisplayName("preserves order")
        void toResponseList_PreservesOrder() {
            TicketComment comment1 = TicketComment.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .text("First")
                    .build();
            TicketComment comment2 = TicketComment.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .text("Second")
                    .build();
            TicketComment comment3 = TicketComment.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .text("Third")
                    .build();

            List<TicketCommentResponse> responses = TicketCommentMapper.toResponseList(
                    List.of(comment1, comment2, comment3)
            );

            assertEquals("First", responses.get(0).getText());
            assertEquals("Second", responses.get(1).getText());
            assertEquals("Third", responses.get(2).getText());
        }

        @Test
        @DisplayName("handles multiple comments from different authors")
        void toResponseList_DifferentAuthors() {
            UUID author1 = UUID.randomUUID();
            UUID author2 = UUID.randomUUID();

            TicketComment comment1 = TicketComment.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(author1)
                    .text("Comment from author 1")
                    .build();
            TicketComment comment2 = TicketComment.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(author2)
                    .text("Comment from author 2")
                    .build();

            List<TicketCommentResponse> responses = TicketCommentMapper.toResponseList(
                    List.of(comment1, comment2)
            );

            assertEquals(author1, responses.get(0).getCommentAuthorId());
            assertEquals(author2, responses.get(1).getCommentAuthorId());
        }

        @Test
        @DisplayName("handles large list")
        void toResponseList_LargeList() {
            List<TicketComment> comments = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                comments.add(TicketComment.builder()
                        .id(UUID.randomUUID())
                        .ticketId(ticketId)
                        .authorId(authorId)
                        .text("Comment " + i)
                        .build());
            }

            List<TicketCommentResponse> responses = TicketCommentMapper.toResponseList(comments);

            assertEquals(100, responses.size());
            assertEquals("Comment 0", responses.get(0).getText());
            assertEquals("Comment 99", responses.get(99).getText());
        }
    }
}
