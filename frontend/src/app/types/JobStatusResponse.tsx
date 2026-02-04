export interface JobStatusResponse {
    status: "PROCESSING" | "COMPLETED" | "ERROR" | "NOT_FOUND";
    progress: number;
    message?: string;
}