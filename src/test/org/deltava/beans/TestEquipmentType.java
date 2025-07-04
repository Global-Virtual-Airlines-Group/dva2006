package org.deltava.beans;

import java.util.*;

public class TestEquipmentType extends AbstractBeanTestCase {

    private EquipmentType _eq;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _eq = new EquipmentType("CRJ-200");
        setBean(_eq);
    }

    @Override
	protected void tearDown() throws Exception {
        _eq = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("CRJ-200", _eq.getName());
        assertTrue(_eq.getActive());
        assertEquals(_eq.getName(), _eq.toString());
        checkProperty("CPID", Integer.valueOf(1234));
        checkProperty("CPName", "CPNAME");
        checkProperty("CPEmail", "root@localhost");
        checkProperty("stage", Integer.valueOf(5));
        checkProperty("ACARSPromotionLegs", Boolean.TRUE);
        _eq.setActive(false);
        assertFalse(_eq.getActive());
    }
    
    public void testValidation() {
        validateInput("CPID", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("stage", Integer.valueOf(-1), IllegalArgumentException.class);
        try {
            _eq.addRank(null);
            fail("NullPointerException expected");
        } catch (NullPointerException npe) {
        	// empty
        }
    }
    
    public void testComboAlias() {
        assertEquals(_eq.getName(), _eq.getComboName());
        assertEquals(_eq.getName(), _eq.getComboAlias());
    }
    
    public void testRanks() {
        assertNotNull(_eq.getRanks());
        assertEquals(0, _eq.getRanks().size());
        _eq.addRank(Rank.FO);
        assertEquals(1, _eq.getRanks().size());
        assertTrue(_eq.getRanks().contains(Rank.FO));
        
        _eq.addRanks("Senior Captain,Chief Pilot", ",");
        assertEquals(4, _eq.getRanks().size());
        
        // Set ranks via array
        _eq.setRanks(Arrays.asList(new String[] {"First Officer", "Captain"}));
        assertEquals(2, _eq.getRanks().size());
        assertTrue(_eq.getRanks().contains(Rank.FO));
        assertTrue(_eq.getRanks().contains(Rank.C));
    }
    
    public void testPromotionInfo() {
        assertEquals(0, _eq.getPromotionHours());
        assertEquals(0, _eq.getPromotionLegs());
        _eq.setPromotionHours(10);
        _eq.setPromotionLegs(10);
        assertEquals(10, _eq.getPromotionHours());
        assertEquals(10, _eq.getPromotionLegs());
        try {
            _eq.setPromotionHours(-10);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
        	// empty
        }
        
        try {
            _eq.setPromotionLegs(-10);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
        	// empty
        }
    }
    
    public void testRatings() {
        assertNotNull(_eq.getPrimaryRatings());
        assertNotNull(_eq.getSecondaryRatings());
        assertEquals(0, _eq.getPrimaryRatings().size());
        assertEquals(0, _eq.getSecondaryRatings().size());
        _eq.addPrimaryRating("B757-200");
        _eq.addSecondaryRating("CRJ-700");
        _eq.addSecondaryRating("EMB-120");
        assertEquals(2, _eq.getSecondaryRatings().size());
        
        // Check to ensure that adding a PRating removes an SRating
        _eq.addPrimaryRating("CRJ-700");
        assertEquals(2, _eq.getPrimaryRatings().size());
        assertEquals(1, _eq.getSecondaryRatings().size());
        assertFalse(_eq.getSecondaryRatings().contains("CRJ-700"));
        
        // Check to ensure that adding an SRating removes a PRating
        _eq.addSecondaryRating("CRJ-700");
        assertEquals(2, _eq.getSecondaryRatings().size());
        assertEquals(1, _eq.getPrimaryRatings().size());
        assertFalse(_eq.getPrimaryRatings().contains("CRJ-700"));
        
        // Set ratings via array
        _eq.setRatings(Arrays.asList(new String[] {"B727-100", "B727-200"}), Arrays.asList(new String[] {"TU-154" }));
        assertEquals(2, _eq.getPrimaryRatings().size());
        assertTrue(_eq.getPrimaryRatings().contains("B727-100"));
        assertTrue(_eq.getPrimaryRatings().contains("B727-200"));
        assertEquals(1, _eq.getSecondaryRatings().size());
        assertTrue(_eq.getSecondaryRatings().contains("TU-154"));
    }
    
    public void testComparison() {
    	assertEquals(_eq.hashCode(), _eq.getName().hashCode());
        EquipmentType eq2 = new EquipmentType("CRJ-200");
        assertEquals(_eq, eq2);
        assertEquals(0, _eq.compareTo(eq2));
        EquipmentType eq3 = new EquipmentType("B737-800");
        eq3.setStage(2);
        assertEquals(-1, _eq.compareTo(eq3));
        assertFalse(_eq.equals(new Object()));
        assertFalse(_eq.equals(null));
    }
    
    @SuppressWarnings("unlikely-arg-type")
	public void testIndexOf() {
       List<EquipmentType> eqTypes = new ArrayList<EquipmentType>();
       eqTypes.add(_eq);
       assertTrue(_eq.equals(_eq.getName()));
       assertTrue(eqTypes.contains(_eq));
       assertTrue(eqTypes.contains(new EquipmentType(_eq.getName())));
    }
}
