import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { DocuMindService } from "../service/DocuMindService";
import { ChatHeader } from "../components/ChatHeader";
import { ChatMessageList } from "../components/ChatMessageList";
import { ChatInput } from "../components/ChatInput";
import type { Message } from "../types/message";

export function ChatPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [inputValue, setInputValue] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  // Estado inicial
  const [messages, setMessages] = useState<Message[]>([
    {
      id: "init",
      role: "ai",
      content: t("chat.initialMessage"),
    },
  ]);

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputValue.trim() || isLoading) return;

    const userQuestion = inputValue.trim();
    setInputValue(""); 
    setIsLoading(true);

    // 1. Añadir mensaje usuario
    const newUserMsg: Message = {
      id: Date.now().toString(),
      role: "user",
      content: userQuestion,
    };
    setMessages((prev) => [...prev, newUserMsg]);

    try {
      // 2. Llamada Backend
      const answer = await DocuMindService.sendQuestion(userQuestion);

      if (!answer || !answer.trim()) {
        throw new Error("INFO_NOT_FOUND");
      }

      // 3. Añadir respuesta IA
      const newAiMsg: Message = {
        id: (Date.now() + 1).toString(),
        role: "ai",
        content: answer,
      };
      setMessages((prev) => [...prev, newAiMsg]);

    } catch (error: any) {
      console.error("Error en chat:", error);

      if (error.message === "INFO_NOT_FOUND") {
        const notFoundMsg: Message = {
          id: (Date.now() + 1).toString(),
          role: "ai",
          content: t("chat.answerNotFound"),
        };
        setMessages((prev) => [...prev, notFoundMsg]);
      } else {
        let errorText = t("chat.errorGeneric");
        if(error.message === "Backend Unreachable") {
            errorText = "No puedo conectar con el cerebro de DocuMind (Backend apagado).";
        }
        toast.error("Error de comunicación", { description: errorText });
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleCloseSession = () => {
    navigate("/");
  };

  return (
    <div className="chat-layout">
      
      <ChatHeader onClose={handleCloseSession} />

      <ChatMessageList 
        messages={messages} 
        isLoading={isLoading} 
      />

      <ChatInput 
        inputValue={inputValue}
        setInputValue={setInputValue}
        onSend={handleSendMessage}
        isLoading={isLoading}
      />
      
    </div>
  );
}