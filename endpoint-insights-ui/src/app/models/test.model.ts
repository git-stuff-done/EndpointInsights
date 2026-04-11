import {JobStatus} from '../common/job.constants';
import {UserInfo} from './user.model';

export interface TestItem {
    jobId: string;
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
    jmeterTestName?: string;
    jobType: string;
    createdAt: Date | string;
    createdBy: UserInfo;
    status: JobStatus;
    threshold: number;
}

const mockUserInfo: UserInfo = {
    name: 'Test User',
    email: 'test@example.com',
    role: 'EDITOR',
    issuer: 'https://auth.example.com',
    subject: 'test-user-123'
};

export const MOCK_TESTS: TestItem[] = [
    {
        jobId: '0b00f357-a21f-45b0-b753-b791b4f83b8d',
        name: 'Auth',
        batch: 'Nightly-01',
        createdAt: new Date(),
        createdBy: mockUserInfo,
        status: 'RUNNING',
        gitUrl: 'https://git.com/test',
        description: 'this is a test',
        jobType: 'E2E',
        compileCommand: './ep-compile <testname>',
        runCommand: './ep-run <testname> -<type>',
        threshold: 99,
    },
    {
        jobId: '89bb3ff9-08f1-49e3-ab61-36b5cbb42dd7',
        name: 'Billing',
        batch: 'Nightly-01',
        createdAt: new Date(),
        createdBy: mockUserInfo,
        status: 'STOPPED',
        gitUrl: 'https://git.com/test',
        description: '',
        jobType: 'nightwatch',
        compileCommand: '',
        runCommand: './ep-run <testname> -<type>',
        threshold: 199,
    }
];
