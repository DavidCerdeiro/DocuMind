import type { ProgressBarProps } from "../types/ProgressBarProps";

export function ProgressBar({ progress, label }: ProgressBarProps) {
  return (
    <div className="progress-container">
      <div className="progress-info">
        <span>{label || "Loading..."}</span>
        <span>{progress}%</span>
      </div>
      
      <div className="progress-track">
        <div 
          className="progress-fill" 
          style={{ width: `${progress}%` }}
        ></div>
      </div>
    </div>
  );
}