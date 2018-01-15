package org.fenixedu.ulisboa.reports.ui;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.spring.FenixEDUBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.google.gson.GsonBuilder;

public class FenixeduULisboaReportsBaseController extends FenixEDUBaseController {

    //The HTTP Request that can be used internally in the controller
    protected @Autowired HttpServletRequest request;

    @ModelAttribute
    protected void addModelProperties(Model model) {
        super.addModelProperties(model, request);

        String infoMessages = request.getParameter(INFO_MESSAGES);
        if (infoMessages != null) {
            addInfoMessage(infoMessages, model);
        }
        String warningMessages = request.getParameter(WARNING_MESSAGES);
        if (warningMessages != null) {
            addWarningMessage(warningMessages, model);
        }
        String errorMessages = request.getParameter(ERROR_MESSAGES);
        if (errorMessages != null) {
            addErrorMessage(errorMessages, model);
        }

    }

    @Override
    protected void registerTypeAdapters(GsonBuilder builder) {
        super.registerTypeAdapters(builder);
    }

    protected void writeFile(final HttpServletResponse response, final String filename, final String contentType,
            final byte[] content) throws IOException {

        response.setContentLength(content.length);
        response.setContentType(contentType);
        response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(content);
            outputStream.flush();
            response.flushBuffer();
        }
    }

    protected static class SecToken {
        private static String SEED = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-";
        private static int DEFAULT_KEY_SIZE = 16;

        public static String generate(int keySize) {
            char[] key = new char[keySize];
            for (int i = 0; i < keySize; i++) {
                key[i] = SEED.charAt(ThreadLocalRandom.current().nextInt(0, (SEED.length() - 1)));
            }
            return String.valueOf(key);
        }

        public static String generate() {
            return generate(DEFAULT_KEY_SIZE);
        }
    }
}
