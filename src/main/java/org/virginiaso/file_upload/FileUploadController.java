package org.virginiaso.file_upload;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.virginiaso.file_upload.util.FieldValidationException;
import org.virginiaso.file_upload.util.NoSuchEventException;

@Controller
public class FileUploadController {
	private static final Logger LOG = LoggerFactory.getLogger(FileUploadController.class);

	private final FileUploadService fileUploadService;

	@Autowired
	public FileUploadController(FileUploadService fileUploadService) {
		this.fileUploadService = fileUploadService;
	}

	@GetMapping({"/", "/fileUpload"})
	public String homePage(Model model) {
		return "index";
	}

	@GetMapping("/fileUpload/{eventTemplate}")
	public String fileUploadForm(
		@PathVariable("eventTemplate") String eventTemplate,
		Model model) throws NoSuchEventException {

		model.addAttribute("event", Event.forTemplate(eventTemplate));
		model.addAttribute("userSub", new UserSubmission());
		return eventTemplate;
	}

	@PostMapping("/fileUpload/{eventTemplate}")
	public String fileUploadSubmit(
		@PathVariable("eventTemplate") String eventTemplate,
		@ModelAttribute UserSubmission userSub,
		@RequestParam(name = "fileA", required = false) MultipartFile fileA,
		@RequestParam(name = "fileB", required = false) MultipartFile fileB,
		@RequestParam(name = "fileC", required = false) MultipartFile fileC,
		@RequestParam(name = "fileD", required = false) MultipartFile fileD,
		@RequestParam(name = "fileE", required = false) MultipartFile fileE,
		@RequestParam(name = "fileF", required = false) MultipartFile fileF,
		@RequestParam(name = "fileG", required = false) MultipartFile fileG,
		@RequestParam(name = "fileH", required = false) MultipartFile fileH,
		@RequestParam(name = "fileI", required = false) MultipartFile fileI,
		@RequestParam(name = "fileJ", required = false) MultipartFile fileJ,
		HttpServletRequest request,
		Model model) throws IOException, NoSuchEventException {

		Submission submission = fileUploadService.receiveFileUpload(eventTemplate,
			userSub, fileA, fileB, fileC, fileD, fileE, fileF, fileG, fileH, fileI, fileJ);

		model.addAttribute("event", submission.getEvent());
		model.addAttribute("submission", submission);
		if (submission.getEvent() != Event.HELICOPTER_START) {
			return "submissionResult";
		} else {
			String submitUrl = request.getRequestURL().toString().replace(
				Event.HELICOPTER_START.getTemplateName(),
				Event.HELICOPTER_FINISH.getTemplateName());
			model.addAttribute("submitUrl", submitUrl);
			return "helicopterGo";
		}
	}

	@ExceptionHandler
	public String handleIOException(Model model, IOException ex) {
		return handleException(model, ex);
	}

	@ExceptionHandler
	public String handleNoSuchEventException(Model model, NoSuchEventException ex) {
		return handleException(model, ex);
	}

	@ExceptionHandler
	public String handleFieldValidationException(Model model, FieldValidationException ex) {
		return handleException(model, ex);
	}

	@ExceptionHandler
	public String handleRuntimeException(Model model, RuntimeException ex) {
		return handleException(model, ex);
	}

	private static String handleException(Model model, Throwable ex) {
		LOG.error("Encountered Exception:", ex);
		model.addAttribute("exception", ex.getClass().getSimpleName());
		model.addAttribute("message", ex.getMessage());
		return "submissionError";
	}
}
