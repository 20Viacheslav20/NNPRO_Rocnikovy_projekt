import { ProjectStatus } from './project-status.enum';

export interface Project {
  id: string;                 //changed to string
  name: string;
  description?: string | null;
  status: ProjectStatus;
  createdAt: string;          //added
}
