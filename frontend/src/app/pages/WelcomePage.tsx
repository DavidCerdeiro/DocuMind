import { useState } from "react"; // Añade useRef si quieres controlar mejor el intervalo
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { FileUploader } from "../components/FileUploader";
import { DocuMindService } from "../service/DocuMindService"; 
import { toast } from "sonner";

export function WelcomePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  
  // Estado opcional para mostrar mensajes de progreso al usuario
  const [loadingMessage, setLoadingMessage] = useState("Procesando...");

  const handleUpload = async () => {
    if (!selectedFile) return;

    setIsLoading(true);
    setLoadingMessage("Iniciando carga..."); // Feedback inicial

    try {
      // PASO 1: Subir archivo y obtener ID del trabajo
      const { jobId } = await DocuMindService.uploadDocument(selectedFile);
      
      setLoadingMessage("Analizando documento..."); // Cambiamos mensaje

      // PASO 2: Iniciar Polling (Preguntar cada 2 segundos)
      const pollInterval = setInterval(async () => {
        try {
          const status = await DocuMindService.getJobStatus(jobId);
          console.log(`Estado del trabajo ${jobId}: ${status}`);

          if (status === "COMPLETED") {
            // ¡Éxito!
            clearInterval(pollInterval);
            toast.success(t("welcome.file.success") || "¡Documento listo!");
            navigate("/chat");
            
          } else if (status.startsWith("ERROR") || status === "NOT_FOUND") {
            // Error en el backend durante el proceso
            clearInterval(pollInterval);
            toast.error("Error en el procesamiento", { 
              description: status 
            });
            setIsLoading(false);
          } 
          // Si sigue en PROCESSING, el intervalo continúa...

        } catch (pollError) {
          // Error de red al consultar el estado
          clearInterval(pollInterval);
          console.error(pollError);
          toast.error("Error de conexión verificando estado");
          setIsLoading(false);
        }
      }, 2000); // 2000ms = 2 segundos

    } catch (error: any) {
      // Error inicial al subir el archivo (antes del polling)
      console.error(error);
      let errorMessage = t("welcome.file.uploadErrorDescription");
      
      if (error.message && error.message.includes("Backend Unreachable")) {
          errorMessage = "El servidor parece estar apagado 🔌.";
      }

      toast.error(t("welcome.file.uploadError"), {
        description: errorMessage,
        duration: 5000, 
      });
      setIsLoading(false);
    }
  };

  return (
    <main className="welcome-page-container">
      <h1 className="welcome-title">{t("welcome.title")}</h1>
      <p className="welcome-description">{t("welcome.description")}</p>

      {/* Upload Component */}
      <FileUploader 
        onFileSelect={(file) => setSelectedFile(file)} 
        selectedFileName={selectedFile?.name || null}
        disabled={isLoading}
      />

      {/* Confirmation Button with Loading State */}
      <button
        className="btn-primary flex items-center justify-center gap-2"
        disabled={!selectedFile || isLoading}
        onClick={handleUpload}
      >
        {isLoading ? (
          <>
            <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {/* Mostramos el mensaje dinámico (Iniciando -> Analizando...) */}
            <span>{loadingMessage}</span>
          </>
        ) : (
          t("welcome.confirmationButton")
        )}
      </button>
    </main>
  );
}