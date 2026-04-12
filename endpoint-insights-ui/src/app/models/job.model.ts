import {UserInfo} from "./user.model";

export interface Job {
    jobId: string;
    name: string;
    batch?: string[];
    status?: string;
    type: string;
    compileCommand?: string;
    runCommand?: string;
    gitUrl?: string;
    description?: string;
    config?: string;

    /** Audit fields */
    createdBy?: UserInfo;
    createdDate?: string;
    updatedBy?: UserInfo;
    updatedDate?: string;
}