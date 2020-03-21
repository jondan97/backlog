package gr.university.thesis.service;

import gr.university.thesis.entity.Comment;
import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.User;
import gr.university.thesis.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * service that handles everything relating to the comments stored in the repository
 */
@Service
public class CommentService {

    CommentRepository commentRepository;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param commentRepository: repository that has access to all the comments
     */
    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * this method allows the creation of a comment, and is stored in the repository
     *
     * @param body:  body of the comment, what is says
     * @param item:  item that this belongs to
     * @param owner: user who wrote the comment
     */
    public void createComment(String body, Item item, User owner) {
        Comment comment = new Comment(body, item, owner);
        commentRepository.save(comment);
    }

    /**
     * this method update an existing comment in the repository and saves it
     *
     * @param commentId: the id of the comment that is going to be updated
     * @param body:      body of the comment, what is says
     */
    public void updateComment(long commentId, String body) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            comment.setBody(body);
            commentRepository.save(comment);
        }
    }

    /**
     * this method deletes a comment from the repository
     *
     * @param commentId: the comment id that is needed in order for the comment to be found in the repository
     *                   and to be deleted
     */
    public void deleteComment(long commentId) {
        commentRepository.deleteById(commentId);
    }
}
