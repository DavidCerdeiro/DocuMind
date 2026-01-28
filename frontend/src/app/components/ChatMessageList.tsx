import { useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import type { Message } from "../types/message"; 

interface ChatMessageListProps {
  messages: Message[];
  isLoading: boolean;
}

export function ChatMessageList({ messages, isLoading }: ChatMessageListProps) {
  const { t } = useTranslation();
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isLoading]);

  return (
    <main className="chat-messages-area">
      <div className="chat-container-limit flex flex-col">
        {messages.map((msg) => (
          <div key={msg.id} className="mb-6 w-full flex flex-col">
            <span
              className={`text-xs text-slate-400 mb-1 ${
                msg.role === "user" ? "self-end mr-1" : "self-start ml-1"
              }`}
            >
              {msg.role === "user" ? t("chat.userName") : t("chat.aiName")}
            </span>

            <div
              className={
                msg.role === "user" ? "chat-bubble-user" : "chat-bubble-ai"
              }
            >
              <p className="whitespace-pre-wrap">{msg.content}</p>
            </div>
          </div>
        ))}

        {/* Loading Skeleton */}
        {isLoading && (
          <div className="mb-6 w-full flex flex-col animate-pulse">
            <span className="text-xs text-slate-400 mb-1 self-start ml-1">
              {t("chat.aiName")}
            </span>
            <div className="chat-bubble-ai flex gap-1 items-center h-10 w-16 justify-center">
              <div className="w-2 h-2 bg-slate-400 rounded-full animate-bounce [animation-delay:-0.3s]"></div>
              <div className="w-2 h-2 bg-slate-400 rounded-full animate-bounce [animation-delay:-0.15s]"></div>
              <div className="w-2 h-2 bg-slate-400 rounded-full animate-bounce"></div>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>
    </main>
  );
}