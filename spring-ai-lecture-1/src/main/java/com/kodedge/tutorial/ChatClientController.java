package com.kodedge.tutorial;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatClientController {

    private final ChatClient chatClient;

    private final ChatClient configuredChatClient;

    public ChatClientController(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();

        this.configuredChatClient = chatClient
                .defaultSystem("You are a knowledgeable travel planning assistant. " +
                        "Create detailed itineraries with practical recommendations, " +
                        "budget considerations, and cultural insights.")
                .build();

    }

    @PostMapping("/daily-schedule")
    public String optimizeDailySchedule(@RequestParam String question) {
        return chatClient.prompt().user(question).call().content();
    }

    @PostMapping("/email-draft")
    public String draftEmail(@RequestParam String question) {
        ChatResponse chatResponse = chatClient.prompt().user(question).call().chatResponse();

        String content = chatResponse.getResult().getOutput().toString();
        String metadata = String.format("\n\n[Metadata: Model=%s, Tokens=%d]",
                chatResponse.getMetadata().getModel(),
                chatResponse.getMetadata().getUsage().getTotalTokens());

        System.out.println("chatResponse>>>>" + chatResponse);

        return content + metadata;

    }


    @PostMapping("/meeting-prep")
    public String perpareMeeting(@RequestParam String question) {
        return chatClient.prompt()
                .system("You are a professional meeting preparation assistant." +
                        "Create structured agendas with clear objectives, time allocations, and " +
                        "actionable talking points. Focus on productive outcomes and efficient use of meeting time.")
                .user(question)
                .call()
                .content();
    }


    @PostMapping("/learning-plan")
    public String createdLearningPlan(@RequestParam String question) {
        return chatClient.prompt()
                .system("You are a personal learning and development specialist.")
                .system("Create structured learning plans with clear milestones, resource recommendations, and progress tracking.")
                .system("Adapt your recommendations to individual learning styles and time constraints.")
                .user(question)
                .call()
                .content();
    }


    @PostMapping("/decision-support")
    public String analyseDecision(@RequestParam String question) {
        return chatClient.prompt().options(
                OpenAiChatOptions.builder()
                        .temperature(0.8)
                        .maxTokens(800)
                        .build())
                .user(question).call().content();
    }

    @PostMapping("/travel-planning")
    public String planTravel(@RequestParam String question) {
        return configuredChatClient
                .prompt()
                .user(question)
                .call()
                .content();
    }

}
