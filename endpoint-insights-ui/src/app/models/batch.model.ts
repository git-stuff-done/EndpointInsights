/** Represents a single batch entry shown on the Dashboard. */
export interface Batch {
    /** Unique ID for the batch (UUID) */
    id: string;

    /** Display title (usually a short descriptive name) */
    title: string;

    /** ISO string or timestamp representing when the batch was created or run */
    date: string;

    /** description or notes */
    description?: string;
}
