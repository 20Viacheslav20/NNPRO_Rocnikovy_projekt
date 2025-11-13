import { ProjectStatus } from './project-status.enum';

export interface ProjectRequest {
    name: string;
    description?: string | null;
    status?: ProjectStatus;
}
