
export const JOB_STATUSES = ['RUNNING', 'STOPPED', 'PENDING', 'FAILED', 'SUCCESS'] as const;

export type JobStatus = typeof JOB_STATUSES[number];