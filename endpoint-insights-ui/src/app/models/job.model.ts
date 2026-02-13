
export interface Job {
    id: string;
    name: string;
    batch?: string[];
    status?: string;
    type: string;
    compileCommand?: string;
    runCommand?: string;
    gitUrl?: string;
    description?: string;
    config?: string;

}