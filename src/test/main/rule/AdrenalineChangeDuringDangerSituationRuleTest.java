package main.rule;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import uk.ac.kent.cs.model.event.Adrenaline;
import uk.ac.kent.cs.model.situation.DangerSituation;

public class AdrenalineChangeDuringDangerSituationRuleTest extends RulesTest {

	/**
	 * Verifies that DangerSituation contains only Adrenaline objects inserted after its creation and before it becomes past.
	 * <dl>
	 * <dt>Steps:
	 * <dd>1. Insert adrenaline > THRESHOLD
	 * <dd>2. Insert adrenaline > previous adrenaline
	 * <dd>3. Insert adrenaline > previous adrenaline
	 * <dd>4. Insert adrenaline < THRESHOLD
	 * <dd>5. Insert adrenaline < previous adrenaline
	 * <dd>6. Insert adrenaline > THRESHOLD
	 * <dt>Expected:
	 * <dd>1. KieBase should contain two DangerSituation
	 * <dd>2. DangerSituation should contain the second and third values of inserted Adrenalines.
	 */
	@Test
	public void adrenalineChangeDuringDangerSituation1() {
		System.out.println("[JUnit] Starting test 'adrenalineChangeDuringDangerSituation1'");
		try {
			adrLevel = threshold + 1;
			hippocampus.insert(new Adrenaline(adrLevel), defaultDelay);
			this.checkDangerSituation(1);
			DangerSituation situation = this.getDangerSituation();
			this.situationCheckAdrenalines(situation);
			
			adrLevel = threshold + 2;
			hippocampus.insert(new Adrenaline(adrLevel), defaultDelay);
			this.checkDangerSituation(1);
			situation = this.getDangerSituation();
			this.situationCheckAdrenalines(situation, new Integer[]{7});
			
			adrLevel = threshold + 3;
			hippocampus.insert(new Adrenaline(adrLevel), defaultDelay);
			this.checkDangerSituation(1);
			situation = this.getDangerSituation();
			this.situationCheckAdrenalines(situation, new Integer[]{7,8});

			adrLevel = threshold - 1;
			hippocampus.insert(new Adrenaline(adrLevel), defaultDelay);
			this.checkDangerSituation(1);
			situation = this.getDangerSituation();
			this.situationCheckAdrenalines(situation, new Integer[]{7,8});

			adrLevel = threshold - 2;
			hippocampus.insert(new Adrenaline(adrLevel), defaultDelay);
			this.checkDangerSituation(1);
			situation = this.getDangerSituation();
			this.situationCheckAdrenalines(situation, new Integer[]{7,8});
			
			adrLevel = threshold + 1;
			hippocampus.insert(new Adrenaline(adrLevel), defaultDelay);
			this.checkDangerSituation(2);
			ArrayList<DangerSituation> allSituations = this.getObjects(DangerSituation.class);
			
			int count = 0;
			for(DangerSituation sit : allSituations){
				if(sit.getAdrenalines().isEmpty()){
					count++;
				}else{
					this.situationCheckAdrenalines(sit, new Integer[]{7,8});
					count += 2;
				}
			}
			assertTrue("Adrenaline objects in DangerSituation are incorrect", count == 3);
		}catch(Exception e){
			e.printStackTrace();
			fail("Exception caught");
		}finally{
			System.out.println("[JUnit] Finishing test 'adrenalineChangeDuringDangerSituation1'");
		}
	}

}