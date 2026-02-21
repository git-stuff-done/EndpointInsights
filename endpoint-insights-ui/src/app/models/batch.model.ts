import {Job} from "./job.model";

/** Represents a single batch entry shown on the Dashboard. */
export interface Batch {
    /** Unique ID for the batch (UUID) */
    id: string;

    /** Display title (usually a short descriptive name) */
    batchName: string;

    /** ISO s
     * tring or timestamp representing when the batch was created or run */
    startTime: string | Date;
    lastRunTime?: string | Date;
    lastRunDate?: string | Date;
    nextRunTime?: string | Date;
    nextRunDate?: string | Date;
    scheduledDays?: string[];
    notificationList?: string[];
    description?: string;
    active: boolean;
    jobs: Job[];
}
