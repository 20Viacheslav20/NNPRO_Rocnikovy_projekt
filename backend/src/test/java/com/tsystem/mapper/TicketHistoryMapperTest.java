package com.tsystem.mapper;

import com.tsystem.model.TicketHistory;
import com.tsystem.model.dto.response.TicketHistoryResponse;
import com.tsystem.model.mapper.TicketHistoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TicketHistoryMapperTest {

    private TicketHistory testHistory;
    private UUID historyId;
    private UUID ticketId;
    private UUID authorId;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        historyId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        now = OffsetDateTime.now();

        testHistory = TicketHistory.builder()
                .id(historyId)
                .ticketId(ticketId)
                .authorId(authorId)
                .action("CREATED")
                .field(null)
                .oldValue(null)
                .newValue(null)
                .createdAt(now)
                .build();
    }

    @Nested
    @DisplayName("toResponse Tests")
    class ToResponseTests {

        @Test
        @DisplayName("converts CREATED action correctly")
        void toResponse_CreatedAction() {
            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertNotNull(response);
            assertEquals(historyId, response.getId());
            assertEquals(authorId, response.getAuthorId());
            assertEquals("CREATED", response.getAction());
            assertNull(response.getField());
            assertNull(response.getOldValue());
            assertNull(response.getNewValue());
            assertEquals(now, response.getCreatedAt());
        }

        @Test
        @DisplayName("converts DELETED action correctly")
        void toResponse_DeletedAction() {
            testHistory.setAction("DELETED");

            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertEquals("DELETED", response.getAction());
            assertNull(response.getField());
            assertNull(response.getOldValue());
            assertNull(response.getNewValue());
        }

        @Test
        @DisplayName("converts UPDATED action with name field")
        void toResponse_UpdatedName() {
            testHistory.setAction("UPDATED");
            testHistory.setField("name");
            testHistory.setOldValue("Old Name");
            testHistory.setNewValue("New Name");

            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertEquals("UPDATED", response.getAction());
            assertEquals("name", response.getField());
            assertEquals("Old Name", response.getOldValue());
            assertEquals("New Name", response.getNewValue());
        }

        @Test
        @DisplayName("converts UPDATED action with description field")
        void toResponse_UpdatedDescription() {
            testHistory.setAction("UPDATED");
            testHistory.setField("description");
            testHistory.setOldValue("Old description");
            testHistory.setNewValue("New description");

            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertEquals("description", response.getField());
            assertEquals("Old description", response.getOldValue());
            assertEquals("New description", response.getNewValue());
        }

        @Test
        @DisplayName("converts UPDATED action with priority field")
        void toResponse_UpdatedPriority() {
            testHistory.setAction("UPDATED");
            testHistory.setField("priority");
            testHistory.setOldValue("low");
            testHistory.setNewValue("high");

            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertEquals("priority", response.getField());
            assertEquals("low", response.getOldValue());
            assertEquals("high", response.getNewValue());
        }

        @Test
        @DisplayName("converts UPDATED action with state field")
        void toResponse_UpdatedState() {
            testHistory.setAction("UPDATED");
            testHistory.setField("state");
            testHistory.setOldValue("open");
            testHistory.setNewValue("in_progress");

            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertEquals("state", response.getField());
            assertEquals("open", response.getOldValue());
            assertEquals("in_progress", response.getNewValue());
        }

        @Test
        @DisplayName("handles null oldValue (new field added)")
        void toResponse_NullOldValue() {
            testHistory.setAction("UPDATED");
            testHistory.setField("description");
            testHistory.setOldValue(null);
            testHistory.setNewValue("New description added");

            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertNull(response.getOldValue());
            assertEquals("New description added", response.getNewValue());
        }

        @Test
        @DisplayName("handles null newValue (field cleared)")
        void toResponse_NullNewValue() {
            testHistory.setAction("UPDATED");
            testHistory.setField("description");
            testHistory.setOldValue("Old description");
            testHistory.setNewValue(null);

            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertEquals("Old description", response.getOldValue());
            assertNull(response.getNewValue());
        }

        @Test
        @DisplayName("handles both null oldValue and newValue")
        void toResponse_BothNullValues() {
            testHistory.setAction("UPDATED");
            testHistory.setField("someField");
            testHistory.setOldValue(null);
            testHistory.setNewValue(null);

            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertNull(response.getOldValue());
            assertNull(response.getNewValue());
        }

        @Test
        @DisplayName("preserves createdAt timestamp")
        void toResponse_PreservesTimestamp() {
            OffsetDateTime specificTime = OffsetDateTime.parse("2024-06-15T10:30:00Z");
            testHistory.setCreatedAt(specificTime);

            TicketHistoryResponse response = TicketHistoryMapper.toResponse(testHistory);

            assertEquals(specificTime, response.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("toResponseList Tests")
    class ToResponseListTests {

        @Test
        @DisplayName("converts list of history entries")
        void toResponseList_ConvertsList() {
            TicketHistory history2 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("UPDATED")
                    .field("priority")
                    .oldValue("low")
                    .newValue("high")
                    .createdAt(now.plusMinutes(5))
                    .build();

            List<TicketHistoryResponse> responses = TicketHistoryMapper.toResponseList(
                    List.of(testHistory, history2)
            );

            assertEquals(2, responses.size());
            assertEquals("CREATED", responses.get(0).getAction());
            assertEquals("UPDATED", responses.get(1).getAction());
        }

        @Test
        @DisplayName("empty list returns empty list")
        void toResponseList_EmptyList() {
            List<TicketHistoryResponse> responses = TicketHistoryMapper.toResponseList(Collections.emptyList());

            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }

        @Test
        @DisplayName("single element list")
        void toResponseList_SingleElement() {
            List<TicketHistoryResponse> responses = TicketHistoryMapper.toResponseList(List.of(testHistory));

            assertEquals(1, responses.size());
            assertEquals(historyId, responses.get(0).getId());
        }

        @Test
        @DisplayName("preserves order")
        void toResponseList_PreservesOrder() {
            TicketHistory h1 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("CREATED")
                    .createdAt(now)
                    .build();
            TicketHistory h2 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("UPDATED")
                    .field("name")
                    .createdAt(now.plusMinutes(1))
                    .build();
            TicketHistory h3 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("UPDATED")
                    .field("state")
                    .createdAt(now.plusMinutes(2))
                    .build();
            TicketHistory h4 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("DELETED")
                    .createdAt(now.plusMinutes(3))
                    .build();

            List<TicketHistoryResponse> responses = TicketHistoryMapper.toResponseList(
                    List.of(h1, h2, h3, h4)
            );

            assertEquals("CREATED", responses.get(0).getAction());
            assertEquals("name", responses.get(1).getField());
            assertEquals("state", responses.get(2).getField());
            assertEquals("DELETED", responses.get(3).getAction());
        }

        @Test
        @DisplayName("handles multiple updates to same field")
        void toResponseList_MultipleUpdatesToSameField() {
            TicketHistory update1 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("UPDATED")
                    .field("state")
                    .oldValue("open")
                    .newValue("in_progress")
                    .createdAt(now)
                    .build();
            TicketHistory update2 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("UPDATED")
                    .field("state")
                    .oldValue("in_progress")
                    .newValue("done")
                    .createdAt(now.plusMinutes(10))
                    .build();

            List<TicketHistoryResponse> responses = TicketHistoryMapper.toResponseList(
                    List.of(update1, update2)
            );

            assertEquals(2, responses.size());
            assertEquals("open", responses.get(0).getOldValue());
            assertEquals("in_progress", responses.get(0).getNewValue());
            assertEquals("in_progress", responses.get(1).getOldValue());
            assertEquals("done", responses.get(1).getNewValue());
        }

        @Test
        @DisplayName("handles history from different authors")
        void toResponseList_DifferentAuthors() {
            UUID author1 = UUID.randomUUID();
            UUID author2 = UUID.randomUUID();

            TicketHistory h1 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(author1)
                    .action("CREATED")
                    .createdAt(now)
                    .build();
            TicketHistory h2 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(author2)
                    .action("UPDATED")
                    .field("state")
                    .createdAt(now.plusMinutes(5))
                    .build();

            List<TicketHistoryResponse> responses = TicketHistoryMapper.toResponseList(List.of(h1, h2));

            assertEquals(author1, responses.get(0).getAuthorId());
            assertEquals(author2, responses.get(1).getAuthorId());
        }

        @Test
        @DisplayName("handles full ticket lifecycle")
        void toResponseList_FullLifecycle() {
            TicketHistory created = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("CREATED")
                    .createdAt(now)
                    .build();
            TicketHistory updated1 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("UPDATED")
                    .field("state")
                    .oldValue("open")
                    .newValue("in_progress")
                    .createdAt(now.plusHours(1))
                    .build();
            TicketHistory updated2 = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("UPDATED")
                    .field("state")
                    .oldValue("in_progress")
                    .newValue("done")
                    .createdAt(now.plusHours(2))
                    .build();
            TicketHistory deleted = TicketHistory.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .authorId(authorId)
                    .action("DELETED")
                    .createdAt(now.plusHours(3))
                    .build();

            List<TicketHistoryResponse> responses = TicketHistoryMapper.toResponseList(
                    List.of(created, updated1, updated2, deleted)
            );

            assertEquals(4, responses.size());
            assertEquals("CREATED", responses.get(0).getAction());
            assertEquals("UPDATED", responses.get(1).getAction());
            assertEquals("UPDATED", responses.get(2).getAction());
            assertEquals("DELETED", responses.get(3).getAction());
        }
    }
}
