package elyowon.programers.prgmStudy.week_2;

import java.util.ArrayList;
import java.util.LinkedList;


// 기능이 앞에 있는 기능보다 먼저 개발될수 있다.
// 뒤에있는 기능은 앞에있는 기능이 배포될때 함께 배포됩니다.


public class 기능개발 {

    class Deploy{
        int day;
        int featureCnt;

        public Deploy(int day) {
            this.day = day;
        }

        public Deploy(int day,int featureCnt) {
            this.day = day;
            this.featureCnt = featureCnt;
        }

        public Deploy addFeatureCnt(int cnt){
            return new Deploy(this.day,cnt);
        }

        public int getDay() {
            return day;
        }

        public int getFeatureCnt() {
            return featureCnt;
        }
    }

    public int[] solution(int[] progresses,int[] speeds) {
        int length = progresses.length;
        LinkedList<Deploy> Deploys = new LinkedList<Deploy>();

        for (int i = 0; i < length; i++) {
            int day = needDay(progresses[i],speeds[i]);
            Deploys.add(new Deploy(day));
        }

        ArrayList<Deploy> result = deployFeatureCnt(Deploys);

        return result.stream().mapToInt(deploy -> deploy.getFeatureCnt()).toArray();
    }

    private ArrayList<Deploy> deployFeatureCnt(LinkedList<Deploy> nextDeploys) {

        ArrayList<Deploy> list = new ArrayList<>();
        int count = 1;
        Deploy firstDeploy = nextDeploys.poll();

        while (!nextDeploys.isEmpty()) {
            if (firstDeploy.getDay() >= nextDeploys.peek().getDay()){
                count++;
                nextDeploys.poll();
                continue;
            }
            list.add(firstDeploy.addFeatureCnt(count));
            firstDeploy = nextDeploys.poll();
            count = 1;
        }
        list.add(firstDeploy.addFeatureCnt(count));
        return list;
    }

    private int needDay(int progress,int speed) {
        int moreDay = (100 - progress);
        return moreDay % speed == 0 ? moreDay/ speed : moreDay/ speed +1;
    }
}