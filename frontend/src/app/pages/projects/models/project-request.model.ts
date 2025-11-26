import { ProjectStatus } from './project-status.enum';

export interface ProjectCreateRequest {
  name: string;
  description?: string | null;
}
export interface ProjectUpdateRequest {
  name: string;
  description?: string | null;
  status: ProjectStatus;    // necessary by backend
}


