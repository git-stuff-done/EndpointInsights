import {Job} from "./job.model";

/** Represents a single batch entry shown on the Dashboard. */
export interface Batch {
    /** Unique ID for the batch (UUID) */
    id: string;

    /** Display title (usually a short descriptive name) */
    batchName: string;

    /** ISO string or timestamp representing when the batch was created or run */
    startTime: string | Date;
    lastRunTime?: string | Date;
    cronExpression?: string;
    notificationList?: string[];
    description?: string;
    active: boolean;
    jobs: Job[];
}
