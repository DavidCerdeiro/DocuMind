const DOCS_API_URL = "/api/docs";
const CHAT_API_URL = "/api/chat";

export const DocuMindService = {
  /**
   * Inicia la subida asíncrona del documento.
   * Retorna el ID del trabajo (Job ID) para hacer seguimiento.
   */
  uploadDocument: async (file: File): Promise<{ jobId: string }> => {
    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch(`${DOCS_API_URL}/upload`, {
        method: "POST",
        body: formData,
      });

      if (!response.ok) {
        if (response.status === 504 || response.status === 502) throw new Error("Backend Unreachable");
        if (response.status === 415) throw new Error("Formato no soportado. Solo PDFs.");
        throw new Error(`Upload error: ${response.status} ${response.statusText}`);
      }

      // Ahora esperamos recibir: { "jobId": "...", "status": "PROCESSING" }
      return await response.json();
    } catch (error) {
      console.error("Error in uploadDocument:", error);
      throw error;
    }
  },

  /**
   * Consulta el estado de un trabajo de procesamiento.
   * Retorna el estado actual (PROCESSING, COMPLETED, ERROR...).
   */
  getJobStatus: async (jobId: string): Promise<string> => {
    try {
      const response = await fetch(`${DOCS_API_URL}/status/${jobId}`);
      
      if (!response.ok) {
        throw new Error("Error checking status");
      }

      const data = await response.json();
      return data.status; // "PROCESSING", "COMPLETED" o "ERROR..."
    } catch (error) {
      console.error("Error checking status:", error);
      throw error;
    }
  },

  /**
   * Envía una pregunta al modelo de IA sobre el documento cargado.
   * @param question El texto de la pregunta.
   * @returns La respuesta de la IA como string.
   * @throws Error "INFO_NOT_FOUND" si la IA no tiene la respuesta en el doc.
   */
  sendQuestion: async (question: string): Promise<string> => {
    try {
      const response = await fetch(CHAT_API_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ question }),
      });

      if (response.status === 404) {
        throw new Error("INFO_NOT_FOUND");
      }

      if (!response.ok) {
         if (response.status === 504 || response.status === 502) {
            throw new Error("Backend Unreachable");
         }
         throw new Error(`Chat error: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      return data.answer;

    } catch (error) {
      console.error("Error in sendQuestion:", error);
      throw error;
    }
  }
};