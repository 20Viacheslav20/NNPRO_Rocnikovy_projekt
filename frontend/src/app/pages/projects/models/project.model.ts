import { ProjectStatus } from './project-status.enum';

export interface Project {
  id: number;
  name: string;
  description?: string | null;
  status: ProjectStatus;
}
