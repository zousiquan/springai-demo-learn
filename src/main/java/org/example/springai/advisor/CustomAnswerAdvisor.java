package org.example.springai.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomAnswerAdvisor implements BaseAdvisor {
    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("""
			{query}

			Context information is below, surrounded by ---------------------

			---------------------
			{question_answer_context}
			---------------------

			Given the context and provided history information and not prior knowledge,
			reply to the user comment. If the answer is not in the context, inform
			the user that you can't answer the question.
			""");
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        if(chatClientRequest.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS) != null){
            List<Document> documents = (List<Document>) chatClientRequest.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
            String documentContext = documents.stream()
                    .map(document -> {
                        String fileName = (String)document.getMetadata().get("fileName");
                        return "文件名：" + fileName + System.lineSeparator() + document.getText();
                    })
                    .collect(Collectors.joining(System.lineSeparator()));
        UserMessage userMessage = chatClientRequest.prompt().getUserMessage();
        String augmentedUserText = DEFAULT_PROMPT_TEMPLATE
                .render(Map.of("query", userMessage.getText(), "question_answer_context", documentContext))
                + "回答时请给出参考文件的来源，文件名会通过fileName提供给你,显示格式为：\n ----- 参考文件：xxx.txt ";
        // 4. Update ChatClientRequest with augmented prompt.
        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
                .context(chatClientRequest.context())
                .build();
        }
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE - 1;
    }
}
