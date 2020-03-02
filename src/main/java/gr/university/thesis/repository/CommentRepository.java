package gr.university.thesis.repository;

import gr.university.thesis.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository that manages everything that has to do with comments
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
