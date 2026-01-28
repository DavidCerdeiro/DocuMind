import { useTranslation } from "react-i18next";

interface ChatHeaderProps {
  onClose: () => void;
}

export function ChatHeader({ onClose }: ChatHeaderProps) {
  const { t } = useTranslation();

  return (
    <header className="chat-header">
      <div className="chat-container-limit flex justify-between items-center w-full">
        <h1 className="text-lg font-bold text-slate-800 flex items-center gap-2">
          <span className="text-2xl">🧠</span>
          <span className="hidden sm:inline">DocuMind</span>
          <span className="sm:hidden">Chat</span>
        </h1>

        <button
          onClick={onClose}
          className="text-sm font-medium text-slate-600 hover:text-red-600 transition-colors px-3 py-1.5 rounded-lg hover:bg-slate-100"
        >
          {t("chat.closeSession")}
        </button>
      </div>
    </header>
  );
}