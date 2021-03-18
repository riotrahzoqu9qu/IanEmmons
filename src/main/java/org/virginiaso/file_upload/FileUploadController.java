package org.virginiaso.file_upload;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileUploadController {
	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping({"/", "/fileUpload"})
	public String homePage(Model model) {
		return "index";
	}

	@GetMapping("/fileUpload/{eventTemplate}")
	public String fileUploadForm(
		@PathVariable("eventTemplate") String eventTemplate,
		Model model) {

		model.addAttribute("event", Event.forTemplate(eventTemplate));
		model.addAttribute("userSub", new UserSubmission());
		return eventTemplate;
	}

	@PostMapping("/fileUpload/{eventTemplate}")
	public String fileUploadSubmit(
		@PathVariable("eventTemplate") String eventTemplate,
		@ModelAttribute UserSubmission userSub,
		@RequestParam(name = "fileA") MultipartFile fileA,
		@RequestParam(name = "fileB") MultipartFile fileB,
		Model model) {

		model.addAttribute("event", Event.forTemplate(eventTemplate));
		try {
			Submission submission = storageService.receiveFileUpload(
				eventTemplate, userSub, fileA, fileB);
			model.addAttribute("submission", submission);
			return "submissionResult";
		} catch (IOException ex) {
			model.addAttribute("exception", ex.getClass().getSimpleName());
			model.addAttribute("message", ex.getMessage());
			return "submissionError";
		}
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Object> handleStorageFileNotFound(RuntimeException exc) {
		return ResponseEntity.badRequest().build();
	}
}
