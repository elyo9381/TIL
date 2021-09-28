package elyowon.programers.prgmStudy.week_1;


import java.util.HashSet;
import java.util.LinkedList;

/**
 *
 */
public class L3_210714_방문길이 {
    // 게임 캐릭터가 처음 걸어본 길의 길이 return

    class Point {
        int x;
        int y;

        public Point(int x,int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Point movePoint(String dirs,int idx) {
            char c = dirs.charAt(idx);
            Point point = null;
            if (c == 'U') {
                point = new Point(this.x + -1,this.y + 0);
            }
            if (c == 'D') {
                point = new Point(this.x + 1,this.y + 0);
            }
            if (c == 'L') {
                point = new Point(this.x + 0,this.y + -1);
            }
            if (c == 'R') {
                point = new Point(this.x + 0,this.y + 1);
            }
            return point;
        }
    }

    class Road {
        int startX;
        int startY;
        int endX;
        int endY;

        public Road(int startX,int startY,int endX,int endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Road road = (Road) o;

            if (startX != road.startX) return false;
            if (startY != road.startY) return false;
            if (endX != road.endX) return false;
            return endY == road.endY;
        }

        @Override
        public int hashCode() {

            int result = Integer.hashCode(startX);
            result = 31 * result + Integer.hashCode(startX);
            result = 31 * result + Integer.hashCode(startX);
            result = 31 * result + Integer.hashCode(startX);
            return result;
        }
    }


    public int solution(String dirs) {

        HashSet<Road> visited = new HashSet<>();
        LinkedList<Point> q = new LinkedList<>();

        int idx = 0;
        q.add(new Point(5,5));

        while (!q.isEmpty()) {
            if (idx == dirs.length()) break;
            Point now = q.poll();
            int x = now.getX();
            int y = now.getY();

            Point nextPoint = now.movePoint(dirs,idx);
            idx++;

            int nx = nextPoint.getX();
            int ny = nextPoint.getY();

            if (nx < 0 || ny < 0 || nx > 10 || ny > 10) {
                q.add(new Point(x,y));
                continue;
            }

            visited.add(new Road(x,y,nx,ny));
            visited.add(new Road(nx,ny,x,y));
            q.add(new Point(nx,ny));
        }
        return visited.size() / 2;
    }
}