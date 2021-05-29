package org.virginiaso.file_upload;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.virginiaso.file_upload.util.HostNameUtil;
import org.virginiaso.file_upload.util.ProjectInfo;
import org.virginiaso.file_upload.util.ValidationException;

@Controller
public class FileUploadController {
	private static final Logger LOG = LoggerFactory.getLogger(FileUploadController.class);

	private final FileUploadService fileUploadService;
	private final String baseEventUrl;

	@Autowired
	public FileUploadController(FileUploadService fileUploadService,
		@Value("${fileUpload.baseEventUrl}") String baseEventUrl) {
		this.fileUploadService = fileUploadService;
		this.baseEventUrl = baseEventUrl;
	}

	@GetMapping({"/", "/fileUpload"})
	public String homePage(Model model) {
		model.addAttribute("host", HostNameUtil.getHostName());
		model.addAttribute("cores", Runtime.getRuntime().availableProcessors());
		model.addAttribute("projName", ProjectInfo.getProjName());
		model.addAttribute("projVer", ProjectInfo.getProjVersion());
		return "index";
	}

	@GetMapping("/fileUpload/{eventTemplate}")
	public String fileUploadForm(
		@PathVariable("eventTemplate") String eventTemplate,
		Model model) {

		model.addAttribute("event", Event.forTemplate(eventTemplate));
		model.addAttribute("userSub", new UserSubmission());
		model.addAttribute("errorMessage", null);
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
		Model model) throws IOException {

		try {
			Submission submission = fileUploadService.receiveFileUpload(eventTemplate,
				userSub, fileA, fileB, fileC, fileD, fileE, fileF, fileG, fileH, fileI, fileJ);

			model.addAttribute("event", submission.getEvent());
			model.addAttribute("submission", submission);
			model.addAttribute("errorMessage", null);
			if (submission.getEvent() != Event.HELICOPTER_START) {
				return "submissionResult";
			} else {
				model.addAttribute("submitUrl",
					baseEventUrl + Event.HELICOPTER_FINISH.getTemplateName());
				return "helicopterGo";
			}
		} catch (ValidationException ex) {
			model.addAttribute("event", Event.forTemplate(eventTemplate));
			model.addAttribute("userSub", userSub);
			model.addAttribute("errorMessage", ex.getMessage());
			return eventTemplate;
		}
	}

	@ExceptionHandler
	public String handleIoEx(Model model, IOException ex) {
		return handleException(model, ex);
	}

	@ExceptionHandler
	public String handleRuntimeEx(Model model, RuntimeException ex) {
		return handleException(model, ex);
	}

	private static String handleException(Model model, Throwable ex) {
		LOG.error("Encountered Exception:", ex);
		model.addAttribute("exception", ex.getClass().getSimpleName());
		model.addAttribute("message", ex.getMessage());
		return "submissionError";
	}
}
