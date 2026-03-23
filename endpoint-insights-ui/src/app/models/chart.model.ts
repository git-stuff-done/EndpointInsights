export interface ChartPoint {
  label: string;
  value: number;
  status: string;
}

export interface ChartSeries {
  name: string;
  data: ChartPoint[];
}

export interface ChartResponse {
  title: string;
  xAxis: string;
  series: ChartSeries[];
}