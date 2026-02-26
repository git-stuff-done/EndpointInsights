import { JobStatus } from '../common/job.constants';

export interface TestItem {
    id: string;
    name: string;
    batch: string;
    description: string;
    gitUrl: string;
    gitAuthType?: string;
    gitUsername?: string;
    gitPassword?: string;
    gitSshPrivateKey?: string;
    gitSshPassphrase?: string;
    runCommand: string;
    compileCommand: string;
    jobType: string;
    createdAt: Date | string;
    createdBy: string;
    status: JobStatus;
}

export const MOCK_TESTS: TestItem[] = [
    {
        id: '7c601a1b-9c98-45f6-a8e6-786c0d797fe4',
        name: 'Auth - Login OK',
        batch: 'Nightly-01',
        createdAt: new Date(),
        createdBy: 'Alex',
        status: 'RUNNING',
        gitUrl: 'git.com/test',
        description: 'this is a test',
        jobType: 'E2E',
        compileCommand: './ep-compile <testname>',
        runCommand: './ep-run <testname> -<type>'
    },
    {
        id: '2',
        name: 'Billing - Refund',
        batch: 'Nightly-01',
        createdAt: new Date(),
        createdBy: 'Sam',
        status: 'STOPPED',
        gitUrl: 'git.com/test',
        description: '',
        jobType: 'nightwatch',
        compileCommand: '',
        runCommand: './ep-run <testname> -<type>'
    }
];
