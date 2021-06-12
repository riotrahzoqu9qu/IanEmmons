package org.virginiaso.file_upload;

import java.io.IOException;
import java.util.Optional;

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
import org.virginiaso.file_upload.util.StringUtil;
import org.virginiaso.file_upload.util.ValidationException;

@Controller
public class FileUploadController {
	private static final Logger LOG = LoggerFactory.getLogger(FileUploadController.class);

	private final FileUploadService fileUploadService;
	private final String baseEventUrl;
	private final String teamNumbersUrl;
	private final boolean isStateTournament;

	@Autowired
	public FileUploadController(FileUploadService fileUploadService,
		@Value("${fileUpload.baseEventUrl}") String baseEventUrl,
		@Value("${fileUpload.teamNumbersUrl}") String teamNumbersUrl,
		@Value("${fileUpload.isStateTournament:false}") boolean isStateTournament) {
		this.fileUploadService = fileUploadService;
		this.baseEventUrl = baseEventUrl;
		this.teamNumbersUrl = teamNumbersUrl;
		this.isStateTournament = isStateTournament;
	}

	@GetMapping({"/", "/fileUpload"})
	public String homePage(
		@RequestParam("notesUploadsOnly") Optional<String> notesUploadStr,
		Model model) {

		boolean notesUploadsOnly = StringUtil.interpretOptQueryStrParam(notesUploadStr);

		model.addAttribute("host", HostNameUtil.getHostName());
		model.addAttribute("cores", Runtime.getRuntime().availableProcessors());
		model.addAttribute("projName", ProjectInfo.getProjName());
		model.addAttribute("projVer", ProjectInfo.getProjVersion());
		model.addAttribute("teamNumbersUrl", teamNumbersUrl);
		model.addAttribute("isStateTournament", isStateTournament);
		model.addAttribute("eventList", notesUploadsOnly
			? Event.getNotesUploadEvents()
			: Event.getAllEvents());
		return "index";
	}

	@GetMapping("/fileUpload/{eventUri}")
	public String fileUploadForm(
		@PathVariable String eventUri,
		Model model) {

		Event event = Event.forUri(eventUri);
		model.addAttribute("event", event);
		model.addAttribute("userSub", new UserSubmission());
		model.addAttribute("errorMessage", null);
		model.addAttribute("teamNumbersUrl", teamNumbersUrl);
		model.addAttribute("isStateTournament", isStateTournament);
		return event.getTemplateName();
	}

	@PostMapping("/fileUpload/{eventUri}")
	public String fileUploadSubmit(
		@PathVariable("eventUri") String eventUri,
		@ModelAttribute UserSubmission userSub,
		@RequestParam(required = false) MultipartFile fileA,
		@RequestParam(required = false) MultipartFile fileB,
		@RequestParam(required = false) MultipartFile fileC,
		@RequestParam(required = false) MultipartFile fileD,
		@RequestParam(required = false) MultipartFile fileE,
		@RequestParam(required = false) MultipartFile fileF,
		@RequestParam(required = false) MultipartFile fileG,
		@RequestParam(required = false) MultipartFile fileH,
		@RequestParam(required = false) MultipartFile fileI,
		@RequestParam(required = false) MultipartFile fileJ,
		Model model) throws IOException {

		// If this throws, it's because eventUri is unrecognized.  In this
		// case, we let the exception handler deal with it because we have
		// no template name to return from this method.
		Event event = Event.forUri(eventUri);

		try {
			Submission submission = fileUploadService.receiveFileUpload(event, userSub,
				fileA, fileB, fileC, fileD, fileE, fileF, fileG, fileH, fileI, fileJ);

			model.addAttribute("event", event);
			model.addAttribute("submission", submission);
			model.addAttribute("errorMessage", null);
			model.addAttribute("teamNumbersUrl", teamNumbersUrl);
			model.addAttribute("isStateTournament", isStateTournament);
			if (submission.getEvent() != Event.HELICOPTER_START) {
				return "submissionResult";
			} else {
				model.addAttribute("submitUrl",
					baseEventUrl + Event.HELICOPTER_FINISH.getTemplateName());
				return "helicopterGo";
			}
		} catch (ValidationException ex) {
			model.addAttribute("event", event);
			model.addAttribute("userSub", userSub);
			model.addAttribute("errorMessage", ex.getMessage());
			model.addAttribute("teamNumbersUrl", teamNumbersUrl);
			model.addAttribute("isStateTournament", isStateTournament);
			return event.getTemplateName();
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
