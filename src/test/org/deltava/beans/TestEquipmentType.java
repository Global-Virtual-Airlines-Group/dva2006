package org.deltava.beans;

import java.util.*;

public class TestEquipmentType extends AbstractBeanTestCase {

    private EquipmentType _eq;
    
    protected void setUp() throws Exception {
        super.setUp();
        _eq = new EquipmentType("CRJ-200");
        setBean(_eq);
    }

    protected void tearDown() throws Exception {
        _eq = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("CRJ-200", _eq.getName());
        assertTrue(_eq.getActive());
        assertEquals(_eq.getName(), _eq.toString());
        checkProperty("CPID", new Integer(1234));
        checkProperty("CPName", "CPNAME");
        checkProperty("CPEmail", "root@localhost");
        checkProperty("stage", new Integer(5));
        checkProperty("ACARSPromotionLegs", Boolean.TRUE);
        _eq.setActive(false);
        assertFalse(_eq.getActive());
    }
    
    public void testValidation() {
        validateInput("CPID", new Integer(-1), IllegalArgumentException.class);
        validateInput("stage", new Integer(-1), IllegalArgumentException.class);
        try {
            _eq.addRank(null);
            fail("NullPointerException expected");
        } catch (NullPointerException npe) { }
    }
    
    public void testComboAlias() {
        assertEquals(_eq.getName(), _eq.getComboName());
        assertEquals(_eq.getName(), _eq.getComboAlias());
    }
    
    public void testRanks() {
        assertNotNull(_eq.getRanks());
        assertEquals(0, _eq.getRanks().size());
        _eq.addRank("RANK1");
        assertEquals(1, _eq.getRanks().size());
        assertTrue(_eq.getRanks().contains("RANK1"));
        _eq.addRank("RANK2");
        assertEquals(2, _eq.getRanks().size());
        assertTrue(_eq.getRanks().contains("RANK2"));
        
        _eq.addRanks("RANK3,RANK4", ",");
        assertEquals(4, _eq.getRanks().size());
        assertTrue(_eq.getRanks().contains("RANK3"));
        assertTrue(_eq.getRanks().contains("RANK4"));
        
        // Set ranks via array
        _eq.setRanks(Arrays.asList(new String[] {"First Officer", "Captain"}));
        assertEquals(2, _eq.getRanks().size());
        assertTrue(_eq.getRanks().contains("First Officer"));
        assertTrue(_eq.getRanks().contains("Captain"));
    }
    
    public void testAFVRanks() {
        assertNotNull(_eq.getRanks());
        assertEquals(0, _eq.getRanks().size());
        assertFalse(_eq.hasSO());
        _eq.addRank(Ranks.RANK_SO);
        assertTrue(_eq.hasSO());
    }
    
    public void testExamNames() {
        assertNull(_eq.getExamName(Ranks.ENTRY));
        _eq.setExamName(Ranks.ENTRY, "ENTRYEXAM");
        assertNotNull(_eq.getExamName(Ranks.ENTRY));
        assertEquals("ENTRYEXAM", _eq.getExamName(Ranks.ENTRY));
    }
    
    public void testPromotionInfo() {
        assertEquals(0, _eq.getPromotionHours(Ranks.RANK_FO));
        assertEquals(0, _eq.getPromotionLegs(Ranks.RANK_FO));
        _eq.setPromotionHours(Ranks.RANK_FO, 10);
        _eq.setPromotionLegs(Ranks.RANK_FO, 10);
        assertEquals(10, _eq.getPromotionHours(Ranks.RANK_FO));
        assertEquals(10, _eq.getPromotionLegs(Ranks.RANK_FO));
        try {
            _eq.setPromotionHours(Ranks.RANK_C, -10);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) { }
        
        try {
            _eq.setPromotionLegs(Ranks.RANK_C, -10);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) { }
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
    
    public void testIndexOf() {
       List<EquipmentType> eqTypes = new ArrayList<EquipmentType>();
       eqTypes.add(_eq);
       assertTrue(_eq.equals(_eq.getName()));
       assertTrue(eqTypes.contains(_eq));
       assertTrue(eqTypes.contains(new EquipmentType(_eq.getName())));
    }
}
