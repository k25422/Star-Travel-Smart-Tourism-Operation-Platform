export const zh = {
  allThemes: "全部主题",
  mountain: "山岳",
  island: "海岛",
  culture: "文化",
  leisure: "休闲"
} as const;

export function displayDestination<T>(item: T): T {
  return item;
}

export function displayRoute<T extends { destination?: unknown }>(route: T): T {
  return route;
}

export function displayBooking<T>(booking: T): T {
  return booking;
}
