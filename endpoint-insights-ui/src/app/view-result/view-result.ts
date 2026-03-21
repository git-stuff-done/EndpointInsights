import { Component } from '@angular/core';
import {TestRunService} from "../services/test-run.service";
import {ActivatedRoute, Router} from "@angular/router";
import {timeout} from "rxjs";
import {TestRun} from "../models/test-run.model";

@Component({
  selector: 'app-view-result',
  imports: [],
  templateUrl: './view-result.html',
  styleUrl: './view-result.scss',
})
export class ViewResult {

  public testRun?: TestRun;

  constructor(private testRunService: TestRunService,
              private router: Router,
              private activatedRoute: ActivatedRoute) {


    const state = window.history.state;

    // First try to load from state, otherwise from query param
    if (state.runId) {
      this.getTestRun(state.runId);
    } else {
      activatedRoute.queryParams.subscribe(params => {
        const id = params['id'];
        if (id) {
          this.getTestRun(id);
        }
      });
    }

  }

  private getTestRun(id: string) {
    this.testRunService.getRun(id).subscribe(run => {
      console.log(`Received test run: ${JSON.stringify(run)}`);
      this.testRun = run;
    })
  }


}
