import { useState } from "react";
import type { DragEvent, ChangeEvent } from "react";
import { useTranslation } from "react-i18next";
// Importa useNavigate si ya vas a redirigir
// import { useNavigate } from 'react-router-dom';

export function WelcomePage() {
  const { t } = useTranslation();
  // const navigate = useNavigate();
  
  const [isDragging, setIsDragging] = useState(false);
  
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  // 1. Manage file selection via input
  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      processFile(e.target.files[0]);
    }
  };

  // 2. Manage when a file is dragged OVER the area
  const handleDragOver = (e: DragEvent<HTMLLabelElement>) => {
    e.preventDefault(); 
    setIsDragging(true);
  };

  // 3. Manage when a file is dragged LEAVE the area without dropping
  const handleDragLeave = (e: DragEvent<HTMLLabelElement>) => {
    e.preventDefault();
    setIsDragging(false);
  };

  // 4. Manage when a file is DROPPED
  const handleDrop = (e: DragEvent<HTMLLabelElement>) => {
    e.preventDefault();
    setIsDragging(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      processFile(e.dataTransfer.files[0]);
    }
  };

  // Common logic to validate and save the file
  const processFile = (file: File) => {
    if (file.type !== "application/pdf") {
      alert(t("welcome.file.onlyPdfAlert")); // Or use a toast/notification instead
      return;
    }
    
    setSelectedFile(file);
    console.log("File ready to upload:", file.name);
    
    // Here you could call your upload function automatically if you wanted
    // uploadFile(file);
  };

  return (
    <main className="welcome-page-container">
      <h1 className="welcome-title">{t("welcome.title")}</h1>
      <p className="welcome-description">{t("welcome.description")}</p>

      <div className="upload-group">
        <span className="upload-label-text" id="file-label">
          {t("welcome.uploadLabel")}
        </span>
        
        <label 
          htmlFor="fileUpload" 
          className="group cursor-pointer block" // 'block' ensures it takes full width
          // Drag & Drop events added to the label container
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
            onChange={handleFileChange} // Standard event
            aria-labelledby="file-label"
          />
          
          {/* The visual box changes dynamically if dragging (isDragging)
             or if a file is already selected.
          */}
          <div className={`upload-box ${
            isDragging ? "border-blue-500 bg-blue-50 ring-2 ring-blue-500 ring-offset-2" : ""
          } ${selectedFile ? "border-green-500 bg-green-50" : ""}`}>
            
            <svg 
              className={`w-8 h-8 mb-2 transition-all ${
                isDragging || selectedFile ? "text-blue-500 opacity-100 scale-110" : "opacity-50 group-hover:opacity-100"
              }`}
              fill="none" 
              stroke="currentColor" 
              viewBox="0 0 24 24"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
            </svg>
            
            <span className="text-sm font-medium">
              {selectedFile 
                ? t("welcome.file.name", { fileName: selectedFile.name })
                : isDragging 
                  ? t("welcome.file.dropPrompt")
                  : t("welcome.file.clickPrompt")
              }
            </span>
          </div>
        </label>
      </div>

      <button 
        className="btn-primary"
        disabled={!selectedFile} // Disabled if no file
        onClick={() => {
             // Here would go the final submission logic if not automatic
             if(selectedFile) console.log("Sending...", selectedFile)
        }}
      >
        {t("welcome.confirmationButton")}
      </button>
    </main>
  );
}