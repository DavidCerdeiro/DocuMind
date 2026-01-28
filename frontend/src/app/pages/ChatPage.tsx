import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { DocuMindService } from "../service/DocuMindService";
import { ChatHeader } from "../components/ChatHeader";
import { ChatMessageList } from "../components/ChatMessageList";
import { ChatInput } from "../components/ChatInput";
import type { Message } from "../types/message";
import { ConfirmationModal } from "../components/ConfirmationModal";

export function ChatPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [inputValue, setInputValue] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);

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

    const newUserMsg: Message = {
      id: Date.now().toString(),
      role: "user",
      content: userQuestion,
    };
    setMessages((prev) => [...prev, newUserMsg]);

    try {
      const answer = await DocuMindService.sendQuestion(userQuestion);

      if (!answer || !answer.trim()) {
        throw new Error("INFO_NOT_FOUND");
      }

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
        toast.error(t("chat.error.backendUnreachableTitle"), { description: t("chat.error.backendUnreachableDescription") });
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenModal = () => {
    setIsModalOpen(true);
  };

  const confirmCloseSession = async () => {
    
    setIsModalOpen(false);

    try {
      toast.loading(t("chat.closingSession"), { id: "closing-toast" });
      
      await DocuMindService.resetSession();
      
      setMessages([]); 
      setInputValue("");
      
      toast.dismiss("closing-toast");
      toast.success(t("chat.sessionClosed"));

      navigate("/");

    } catch (error) {
      console.error("Error cerrando sesión:", error);
      toast.dismiss("closing-toast");
      toast.error(t("chat.error.backendUnreachableTitle"), {
        description: t("chat.error.backendUnreachableDescription")
      });
      
      navigate("/");
    }
  };

  return (
    <div className="chat-layout relative">
      
      <ChatHeader onClose={handleOpenModal} />

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
      
      <ConfirmationModal 
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)} 
        onConfirm={confirmCloseSession}
      />

    </div>
  );
}