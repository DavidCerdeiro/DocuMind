import { useState } from "react";
import { useTranslation } from "react-i18next";
import type { DragEvent, ChangeEvent } from "react";
import { toast } from "sonner";

interface FileUploaderProps {
  onFileSelect: (file: File) => void;
  selectedFileName: string | null;
  disabled?: boolean;
}

export function FileUploader({ onFileSelect, selectedFileName, disabled = false }: FileUploaderProps) {
  const { t } = useTranslation();
  const [isDragging, setIsDragging] = useState(false);

  // Handlers for drag & drop and file input
  const handleDragOver = (e: DragEvent<HTMLLabelElement>) => {
    e.preventDefault();
    if (!disabled) setIsDragging(true);
  };

  const handleDragLeave = (e: DragEvent<HTMLLabelElement>) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: DragEvent<HTMLLabelElement>) => {
    e.preventDefault();
    setIsDragging(false);
    if (disabled) return;

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      validateAndPropagate(e.dataTransfer.files[0]);
    }
  };

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      validateAndPropagate(e.target.files[0]);
    }
  };

  const validateAndPropagate = (file: File) => {
    if (file.type !== "application/pdf") {
      toast.error(t("welcome.file.onlyPdfAlert"), {
        description: t("welcome.file.onlyPdfDescription"),
        duration: 4000, 
      });
      return;
    }
    onFileSelect(file);
  };

  const containerClasses = `upload-box ${
    isDragging ? "border-blue-500 bg-blue-50 ring-2 ring-blue-500 ring-offset-2" : ""
  } ${selectedFileName ? "border-green-500 bg-green-50" : ""} ${
    disabled ? "opacity-50 cursor-not-allowed" : ""
  }`;

  return (
    <div className="upload-group">
      <span className="upload-label-text" id="file-label">
        {t("welcome.uploadLabel")}
      </span>

      <label
        htmlFor="fileUpload"
        className={`group block ${!disabled ? "cursor-pointer" : ""}`}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
      >
        <input
          type="file"
          accept=".pdf"
          id="fileUpload"
          name="fileUpload"
          className="sr-input"
          onChange={handleInputChange}
          disabled={disabled}
          aria-labelledby="file-label"
        />

        <div className={containerClasses}>
          <svg
            className={`w-8 h-8 mb-2 transition-all ${
              isDragging || selectedFileName
                ? "text-blue-500 opacity-100 scale-110"
                : "opacity-50 group-hover:opacity-100"
            }`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
            />
          </svg>

          <span className="text-sm font-medium">
              {selectedFileName
                ? t("welcome.file.name", { fileName: selectedFileName })
                : isDragging 
                  ? t("welcome.file.dropPrompt")
                  : t("welcome.file.clickPrompt")
              }
            </span>
        </div>
      </label>
    </div>
  );
}