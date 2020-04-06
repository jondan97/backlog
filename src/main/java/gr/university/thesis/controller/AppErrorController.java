package gr.university.thesis.controller;


import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * this controller handles all the errors that the user does, during processing in the server, if something goes
 * wrong, then this controller handles this error and shows it to the user
 */
@RestController
public class AppErrorController
        implements ErrorController {

    /**
     * this method handles the errors that are happening, and shows them to the user using Rest
     *
     * @param request: the request which contains the status code and the exception that happened
     * @return : returns a Rest notification to the user, informing him of the error
     */
    @RequestMapping("error")
    @ResponseBody
    public String handleError(HttpServletRequest request) {

        String message = "User input caused errors.";

        int httpErrorCode = getErrorCode(request);

        if (httpErrorCode == 403) {
            message = "Forbidden to access.";
        }

        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");

        return String.format("<html><body>" +
                        "<h2>Something went wrong</h2>" +
                        "<div>Status code: <b>%s</b></div>" +
                        "<div>Exception Message: <b>%s</b></div>" +
                        "<body>" +
                        "<form>" +
                        "<input type='button' value='Go to Previous Page' onclick='history.back()'>" +
                        "</form>" +
                        "<body></html>",
                statusCode, exception == null ? message : exception.getCause().getMessage());
    }

    /**
     * this method handles the requests without any exceptions, but direct requests, whenever a user tries to
     * access that page
     *
     * @return : returns the error page
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }

    /**
     * this method takes as input a 'bad' http request and returns the error code that this request is associated with
     *
     * @param httpRequest: the request received from the user
     * @return: returns the error code that was associated with this 'bad' request
     */
    private int getErrorCode(HttpServletRequest httpRequest) {
        return (Integer) httpRequest
                .getAttribute("javax.servlet.error.status_code");
    }
}
