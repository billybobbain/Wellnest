# Wellnest Feature Ideas

Ideas captured January 9, 2026 for weekend development.

---

## 1. Room Information & Dimensions

**Immediate Need**: Track room dimensions for furniture shopping (drapes, bookshelf)

**Basic Features**:
- Room dimensions (length, width, ceiling height)
- Window measurements (for drapes)
- Notes field for details
- Optional photo attachments

**Future Potential - Facility Integration**:
- Facilities could provide standardized room dimensions by room type
- Room availability tracking (# of units available by type)
- Waiting list information and periods
- Floor plan templates families could select from
- Helps families research and plan moves vs. calling around for info

**Scaling**:
1. Personal level: Individual family tracks their loved one's specific room
2. Family level: Families at same facility reference common room types
3. Facility level: Facilities provide official data through the app

---

## 2. Supply Tracking (Consumables)

**Need**: Track items that need regular replenishing without formal alerts

**Use Case**: "I constantly take milk and Dr. B (Dr Pepper clone) to mom"

**Features**:
- Simple running list of items
- Last replenished date/timestamp
- No alerts or notifications (just reference)
- Quick log when you drop off supplies
- View history of replenishments

**UI Considerations**:
- Lightweight, easy to update quickly
- Maybe quick-add favorites for frequently tracked items
- Calendar view of what was brought when?

---

## 3. Calendar Coordination & Shared Calendaring

**Problem**: Family members try to coordinate via email - unreliable

**Solution**: Companion/shared calendar app

**Potential Integration**:
- Leverage TaskNow features (already comfortable with task apps)
- Could integrate appointments from Wellnest with task management
- Shared view of who's visiting when
- Coordinate care tasks across family members

**Questions to Explore**:
- Separate companion app or integrated into Wellnest?
- How does sharing work? (invite codes, email, etc.)
- Sync with existing calendar apps or standalone?

---

## 4. Social & Community Featurespronth

**Context**: "I wave at other adults visiting daily, but don't know anyone. I'm not a support group guy, but message boards or something might be useful."

**Potential Features**:
- Message board/forum for caregivers at same facility
- Optional - don't want to force social interaction
- Low-key alternative to formal support groups
- Connect with other visitors you see regularly

**Considerations**:
- Privacy concerns (facility-specific boards?)
- Moderation needs
- How to prevent it from becoming overwhelming
- Keep it optional and low-pressure

**Questions**:
- Facility-based boards vs. general caregiver community?
- Anonymous or real names?
- Topic-based threads or general discussion?

---

## 5. Meal/Menu Tracking

**Context**: Facility provides menu emails. Had lunch with mom - lemon chicken, mixed veggies, black eyed peas, dinner roll. Food was good for a cafeteria.

**Status**: NEEDS MENU EXAMPLES BEFORE IMPLEMENTATION
- Need to collect several menu emails to understand format
- Plan to use AI/model (MCP server or local) to parse email contents
- Paste email body → auto-extract menu items by day/meal

**Potential Features**:
- Display daily/weekly menu from facility
- Track what meals were eaten together
- Note food quality/preferences
- Plan visits around preferred meals?
- Import from facility email or manual entry

**Use Cases**:
- Know what's being served before visiting
- Track if mom is eating well
- Plan lunch/dinner visits around menu
- Remember what meals she enjoyed

**Questions to Explore**:
- How to get menu data? (email forwarding, manual entry, facility API?)
- Track per meal or just view upcoming menus?
- Log what was actually eaten vs. what was available?
- Dietary restrictions/preferences tracking?
- Photo of meals?
- AI parsing approach: What model? Local vs. cloud?

---

## Implementation Priority

### Priority 1: Room Information & Dimensions
**Why first**: Immediate need for furniture shopping
**Complexity**: Low - add fields to Profile entity
**Time**: Quick implementation
**Value**: Immediate practical benefit

### Priority 2: Supply Tracking (Consumables)
**Why second**: High frequency use, relatively simple
**Complexity**: Low-Medium - new entity, simple UI
**Time**: Few hours
**Value**: Constant use (milk, Dr. B runs)

### Priority 3: Meal/Menu Tracking
**Why third**: Practical feature, medium complexity
**Complexity**: Medium - depends on data source approach
**Time**: Half day to full day
**Value**: Regular use, helps plan visits
**Decision needed**: Manual entry vs. email parsing vs. facility API

### Priority 4: Social & Community Features
**Why fourth**: Complex but potentially high engagement
**Complexity**: Very High - moderation, privacy, facility integration
**Time**: Major effort
**Value**: Nice-to-have, optional
**Note**: Research first, possibly Phase 2

### Priority 5: Calendar Coordination
**Why last**: Biggest architectural lift, needs TaskNow integration
**Complexity**: High - sharing, sync, cross-app integration
**Time**: Multi-day effort
**Value**: High long-term, requires significant design work
**Note**: Prototype before committing

## Architecture Questions

- Where does room info live? Part of Profile? Separate entity?
- Supply tracking - separate feature or part of medications/management?
- Social features - biggest architectural lift, think carefully
- Calendar - integration complexity with TaskNow?
