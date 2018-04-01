package com.graduationproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduationproject.config.Mappers;
import com.graduationproject.logic.exceptions.CodedExceptions;
import com.graduationproject.model.ErrorModule;
import com.graduationproject.model.Setting;
import com.graduationproject.model.Wait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Controller
public class PageController {
    @Autowired
    private SimpMessagingTemplate template;

    @RequestMapping(value = {"/", "/component/{id}"})
    public String index(@PathVariable(value = "id") Integer id) {
        if (id != null && !ComponentsController.components.containsID(id))
            return null;
        return "index";
    }

    @GetMapping("/import")
    public String listUploadedFiles() throws IOException {
        return "import";
    }

    @PostMapping("/import")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            Wait wait = new Wait("Importing setting.");
            new SendWait(template, wait).run();
            try {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                    String json = "";
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json += line + System.lineSeparator();
                    }
                    Setting setting = new ObjectMapper().readValue(json, Setting.class);
                    BoardPropertiesController.boardProperties = setting.getBoardProperties();
                    new SendBoardProperties(template).run();
                    ComponentsController.components.change(setting.getComponents());
                    new SendComponents(template).run();
                } catch (IOException e) {
                    throw new CodedExceptions("Invalid import file", CodedExceptions.IMPORT_EXCEPTION, e);
                }
            } catch (CodedExceptions e) {
                e.printStackTrace();
                new SendError(template, new ErrorModule(e)).run();
            }
            new SendWait(template, wait, true).run();
        }
        return "redirect:/import";
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportSetting(HttpServletResponse response) throws IOException {
        Setting setting = new Setting(Mappers.componentsMapper.map(ComponentsController.components), BoardPropertiesController.boardProperties);
        String settingJSON = new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(setting);
        String filename = "setting.json";
        InputStream inputStream = new ByteArrayInputStream(settingJSON.getBytes(StandardCharsets.UTF_8));
        response.setContentType("application/download");
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + filename + "\""));
        FileCopyUtils.copy(inputStream, response.getOutputStream());
    }


}
