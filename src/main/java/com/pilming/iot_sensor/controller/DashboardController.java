package com.pilming.iot_sensor.controller;

import com.pilming.iot_sensor.service.SensorService;
import com.pilming.iot_sensor.service.SensorStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final SensorService sensorService;
    private final SensorStatusService sensorStatusService;

    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        model.addAttribute("sensors", sensorService.getAllSensors());
        model.addAttribute("sensorStatuses", sensorStatusService.getAllStatuses());
        return "dashboard";
    }
}
