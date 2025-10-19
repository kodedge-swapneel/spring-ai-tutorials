package com.kodedge.tutorial;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PromptController {

    private final ChatClient chatClient;

    @Value("classpath:prompts/travel-itinerary-detailed.st")
    private Resource detailedTemplate;

    @Value("classpath:prompts/travel-itinerary-conditional.st")
    private Resource conditionalTemplate;


    public PromptController(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

    @GetMapping("/itinerary")
    public String getItinerary(@RequestParam String ask) {
        String systemMessageStr = """
                You are an expert travel planner with 15+ years of experience
                in creating personalized itineraries,budget optimization,
                and destination expertise worldwide
                """;
        SystemMessage systemMessage = new SystemMessage(systemMessageStr);

        UserMessage userMessage = new UserMessage(ask);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return chatClient.prompt(prompt).call().content();
    }

    @GetMapping("/basic-itinerary")
    public String basicItinerary(
            @RequestParam String destination,
            @RequestParam int days,
            @RequestParam int budget
    ) {
        PromptTemplate promptTemplate = new PromptTemplate(
                """
                        You are an expert travel planner with 15+ years of experience
                        in creating personalized itineraries,budget optimization,
                        and destination expertise worldwide.
                       \s
                        Create a {days}-day travel itinerary for {destination}:
                        Destination: {destination}
                        Duration: {days} days
                        Budget: ${budget} USD
                        Provide:
                        1. Day-by-day itinerary with activities
                        2. Recommended restaurants
                        3. Budget breakdown
                        4. Cultural tips
               \s"""
        );

        Prompt prompt = promptTemplate.create(Map.of(
                "destination", destination,
                "days", days,
                "budget", budget
        ));

        return  chatClient.prompt(prompt).call().content();
    }

    @GetMapping("/role-based-itinerary")
    public String roleBasedItinerary(
            @RequestParam String destination,
            @RequestParam int days,
            @RequestParam int budget,
            @RequestParam String role
    ) {
        SystemMessage systemMessage = new SystemMessage(
                "You are an " + role + " with expertise in destination planning, " +
                        "budget optimization, and personalized travel recommendations. " +
                        "Provide " +
                        "1. Day-by-day itinerary with activities " +
                        "2. Recommended restaurants " +
                        "3. Budget breakdown " +
                        "4. Cultural tips"
        );

        PromptTemplate userTemplate = new PromptTemplate(
                """
                        Plan a {days}-day travel itinerary for {destination}:
                        Destination: {destination}
                        Duration: {days} days
                        Budget: ${budget} USD
                """
        );

        UserMessage userMessage = new UserMessage(
                userTemplate.render(Map.of(
                        "destination", destination,
                        "days", days,
                        "budget", budget
                ))
        );
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.prompt(prompt).call().content();

    }

    @GetMapping("/resource-template-itinerary")
    public String resourceTemplate(
            @RequestParam String destination,
            @RequestParam int days,
            @RequestParam int budget
    ) {

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(detailedTemplate);
        PromptTemplate promptTemplate = new PromptTemplate(systemPromptTemplate.getTemplate());

        Prompt prompt = promptTemplate.create(Map.of(
                "destination", destination,
                "days", days,
                "budget", budget
        ));

        return chatClient.prompt(prompt).call().content();
    }

    @PostMapping("/conditional-templates")
    public String conditionalTemplates(@RequestBody Map<String, Object> request) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(conditionalTemplate);
        Message systemMessage = systemPromptTemplate.createMessage(request);

        Prompt prompt = new Prompt(systemMessage);
        return chatClient.prompt(prompt).call().content();
    }

    @GetMapping("/custom-template renderer")
    public String customTemplateRenderer(
            @RequestParam String destination,
            @RequestParam int days,
            @RequestParam int budget) {

        StTemplateRenderer stTemplateRenderer = StTemplateRenderer.builder()
                .startDelimiterToken('<')
                .endDelimiterToken('>')
                .build();

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(stTemplateRenderer)
                .template(
                        """
                            You are an expert travel planner
                            "Plan a <days>-day travel itinerary 
                            for <destination> with Budget: $<budget> USD
                        """
                ).build();

        Prompt prompt = promptTemplate.create(Map.of(
                "destination", destination,
                "days", days,
                "budget", budget
        ));

        return chatClient.prompt(prompt).call().content();
    }



}
