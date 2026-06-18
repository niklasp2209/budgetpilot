"use client";

import { FormEvent, useEffect, useState } from "react";
import { ApiError } from "@/shared/api/client";
import {
  addMember,
  fetchMembers,
  removeMember,
  updateMemberRole
} from "@/shared/api/members";
import { fetchMe } from "@/shared/api/organizations";
import {
  assignMemberPermissionGroups,
  createPermissionGroup,
  deletePermissionGroup,
  fetchPermissionGroups,
  updatePermissionGroup
} from "@/shared/api/permissionGroups";
import { hasPermission } from "@/shared/lib/permissions";
import { runInEffectAsync } from "@/shared/lib/runInEffectAsync";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";
import type {
  MembershipRole,
  OrganizationMember,
  OrganizationPermission,
  PermissionGroup
} from "@/shared/types/api";
import { ASSIGNABLE_MEMBERSHIP_ROLES as ROLES, ASSIGNABLE_PERMISSIONS as PERMISSIONS } from "@/shared/types/api";

function togglePermission(
  current: OrganizationPermission[],
  permission: OrganizationPermission
): OrganizationPermission[] {
  return current.includes(permission)
    ? current.filter((entry) => entry !== permission)
    : [...current, permission];
}

function toggleGroupId(current: string[], groupId: string): string[] {
  return current.includes(groupId)
    ? current.filter((entry) => entry !== groupId)
    : [...current, groupId];
}

export function MembersView() {
  const { selectedOrganization } = useOrganization();
  const [members, setMembers] = useState<OrganizationMember[]>([]);
  const [groups, setGroups] = useState<PermissionGroup[]>([]);
  const [currentUserId, setCurrentUserId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const canManageMembers = hasPermission(selectedOrganization, "MEMBERS_MANAGE");
  const canManageGroups = hasPermission(selectedOrganization, "PERMISSION_GROUPS_MANAGE");

  const [memberEmail, setMemberEmail] = useState("");
  const [memberPassword, setMemberPassword] = useState("");
  const [memberRole, setMemberRole] = useState<MembershipRole>("MEMBER");

  const [groupName, setGroupName] = useState("");
  const [groupPermissions, setGroupPermissions] = useState<OrganizationPermission[]>([]);
  const [editingGroupId, setEditingGroupId] = useState<string | null>(null);
  const [editGroupName, setEditGroupName] = useState("");
  const [editGroupPermissions, setEditGroupPermissions] = useState<OrganizationPermission[]>([]);

  async function loadData() {
    if (!selectedOrganization) {
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const memberPromise = fetchMembers(selectedOrganization.id);
      const mePromise = fetchMe();
      const groupPromise = canManageGroups
        ? fetchPermissionGroups(selectedOrganization.id)
        : Promise.resolve([] as PermissionGroup[]);

      const [loadedMembers, me, loadedGroups] = await Promise.all([
        memberPromise,
        mePromise,
        groupPromise
      ]);
      setMembers(loadedMembers);
      setCurrentUserId(me.id);
      setGroups(loadedGroups);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to load members.");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    const organizationId = selectedOrganization?.id;
    if (!organizationId) {
      return;
    }

    return runInEffectAsync(async (isCancelled) => {
      setIsLoading(true);
      setError(null);
      try {
        const [loadedMembers, me, loadedGroups] = await Promise.all([
          fetchMembers(organizationId),
          fetchMe(),
          canManageGroups
            ? fetchPermissionGroups(organizationId)
            : Promise.resolve([] as PermissionGroup[])
        ]);
        if (isCancelled()) {
          return;
        }
        setMembers(loadedMembers);
        setCurrentUserId(me.id);
        setGroups(loadedGroups);
      } catch (caught) {
        if (!isCancelled()) {
          setError(caught instanceof ApiError ? caught.message : "Failed to load members.");
        }
      } finally {
        if (!isCancelled()) {
          setIsLoading(false);
        }
      }
    });
  }, [selectedOrganization?.id, canManageGroups]);

  async function handleAddMember(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    try {
      await addMember(selectedOrganization.id, {
        email: memberEmail.trim(),
        password: memberPassword.trim() || undefined,
        role: memberRole
      });
      setMemberEmail("");
      setMemberPassword("");
      setMemberRole("MEMBER");
      await loadData();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to add member.");
    }
  }

  async function handleRoleChange(member: OrganizationMember, role: MembershipRole) {
    if (!selectedOrganization || member.role === role) {
      return;
    }
    setError(null);
    try {
      await updateMemberRole(selectedOrganization.id, member.userId, role);
      await loadData();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to update role.");
    }
  }

  async function handleRemoveMember(userId: string) {
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    try {
      await removeMember(selectedOrganization.id, userId);
      await loadData();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to remove member.");
    }
  }

  async function handleMemberGroupsChange(member: OrganizationMember, groupIds: string[]) {
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    try {
      await assignMemberPermissionGroups(selectedOrganization.id, member.userId, groupIds);
      await loadData();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to update permission groups.");
    }
  }

  async function handleCreateGroup(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization) {
      return;
    }
    if (groupPermissions.length === 0) {
      setError("Select at least one permission for the group.");
      return;
    }
    setError(null);
    try {
      await createPermissionGroup(selectedOrganization.id, groupName.trim(), groupPermissions);
      setGroupName("");
      setGroupPermissions([]);
      await loadData();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to create permission group.");
    }
  }

  function startEditingGroup(group: PermissionGroup) {
    setEditingGroupId(group.id);
    setEditGroupName(group.name);
    setEditGroupPermissions([...group.permissions]);
  }

  async function handleSaveGroup(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization || !editingGroupId) {
      return;
    }
    if (editGroupPermissions.length === 0) {
      setError("Select at least one permission for the group.");
      return;
    }
    setError(null);
    try {
      await updatePermissionGroup(
        selectedOrganization.id,
        editingGroupId,
        editGroupName.trim(),
        editGroupPermissions
      );
      setEditingGroupId(null);
      await loadData();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to update permission group.");
    }
  }

  async function handleDeleteGroup(groupId: string) {
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    try {
      await deletePermissionGroup(selectedOrganization.id, groupId);
      if (editingGroupId === groupId) {
        setEditingGroupId(null);
      }
      await loadData();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to delete permission group.");
    }
  }

  if (!selectedOrganization) {
    return null;
  }

  if (isLoading) {
    return <p className="muted">Loading members...</p>;
  }

  return (
    <div className="stack">
      {error ? <p className="error">{error}</p> : null}

      {canManageMembers ? (
        <section className="card card-wide">
          <h2>Add member</h2>
          <p className="muted">
            Create a new login or add an existing user by email. No invitation email is sent.
          </p>
          <form className="stack-form" onSubmit={handleAddMember}>
            <div className="inline-form">
              <input
                type="email"
                placeholder="Email"
                value={memberEmail}
                onChange={(event) => setMemberEmail(event.target.value)}
                required
              />
              <input
                type="password"
                placeholder="Password (new users only)"
                value={memberPassword}
                onChange={(event) => setMemberPassword(event.target.value)}
                minLength={8}
              />
              <select
                value={memberRole}
                onChange={(event) => setMemberRole(event.target.value as MembershipRole)}
              >
                {ROLES.map((role) => (
                  <option key={role} value={role}>
                    {role}
                  </option>
                ))}
              </select>
            </div>
            <button type="submit" className="inline-button">
              Add member
            </button>
          </form>
        </section>
      ) : null}

      <section className="card card-wide">
        <h2>Members</h2>
        {members.length === 0 ? (
          <p className="muted">No members yet.</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                {canManageGroups ? <th>Permission groups</th> : null}
                {canManageMembers ? <th /> : null}
              </tr>
            </thead>
            <tbody>
              {members.map((member) => {
                const isSelf = member.userId === currentUserId;
                const isOwner = member.role === "OWNER";
                return (
                  <tr key={member.userId}>
                    <td>{member.email}</td>
                    <td>
                      {canManageMembers && !isSelf && !isOwner ? (
                        <select
                          value={member.role}
                          onChange={(event) =>
                            void handleRoleChange(member, event.target.value as MembershipRole)
                          }
                        >
                          {ROLES.map((role) => (
                            <option key={role} value={role}>
                              {role}
                            </option>
                          ))}
                        </select>
                      ) : (
                        <span className="role-static">{member.role}</span>
                      )}
                    </td>
                    <td>{member.status}</td>
                    {canManageGroups ? (
                      <td>
                        {groups.length === 0 ? (
                          <span className="muted">No groups</span>
                        ) : (
                          <div className="checkbox-group">
                            {groups.map((group) => (
                              <label key={group.id} className="checkbox-label">
                                <input
                                  type="checkbox"
                                  checked={member.permissionGroupIds.includes(group.id)}
                                  onChange={() =>
                                    void handleMemberGroupsChange(
                                      member,
                                      toggleGroupId(member.permissionGroupIds, group.id)
                                    )
                                  }
                                />
                                {group.name}
                              </label>
                            ))}
                          </div>
                        )}
                      </td>
                    ) : null}
                    {canManageMembers ? (
                      <td>
                        {!isSelf && !isOwner ? (
                          <div className="row-actions">
                            <button
                              type="button"
                              className="danger-button"
                              onClick={() => void handleRemoveMember(member.userId)}
                            >
                              Remove
                            </button>
                          </div>
                        ) : null}
                      </td>
                    ) : null}
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </section>

      {canManageGroups ? (
        <section className="card card-wide">
          <h2>Permission groups</h2>
          <p className="muted">
            Custom groups add permissions on top of each member&apos;s role.
          </p>

          {groups.length === 0 ? (
            <p className="muted">No permission groups yet.</p>
          ) : (
            <ul className="group-list">
              {groups.map((group) => (
                <li key={group.id} className="group-list-item">
                  {editingGroupId === group.id ? (
                    <form className="stack-form" onSubmit={handleSaveGroup}>
                      <input
                        type="text"
                        value={editGroupName}
                        onChange={(event) => setEditGroupName(event.target.value)}
                        required
                      />
                      <div className="checkbox-group">
                        {PERMISSIONS.map((permission) => (
                          <label key={permission} className="checkbox-label">
                            <input
                              type="checkbox"
                              checked={editGroupPermissions.includes(permission)}
                              onChange={() =>
                                setEditGroupPermissions(togglePermission(editGroupPermissions, permission))
                              }
                            />
                            {permission}
                          </label>
                        ))}
                      </div>
                      <div className="row-actions">
                        <button type="submit" className="inline-button">
                          Save
                        </button>
                        <button
                          type="button"
                          className="secondary-button"
                          onClick={() => setEditingGroupId(null)}
                        >
                          Cancel
                        </button>
                      </div>
                    </form>
                  ) : (
                    <>
                      <div>
                        <strong>{group.name}</strong>
                        <p className="muted permission-tags">
                          {group.permissions.join(", ")}
                        </p>
                      </div>
                      <div className="row-actions">
                        <button
                          type="button"
                          className="secondary-button"
                          onClick={() => startEditingGroup(group)}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          className="danger-button"
                          onClick={() => void handleDeleteGroup(group.id)}
                        >
                          Delete
                        </button>
                      </div>
                    </>
                  )}
                </li>
              ))}
            </ul>
          )}

          <form className="stack-form" onSubmit={handleCreateGroup}>
            <h3>Create group</h3>
            <input
              type="text"
              placeholder="Group name"
              value={groupName}
              onChange={(event) => setGroupName(event.target.value)}
              required
            />
            <div className="checkbox-group">
              {PERMISSIONS.map((permission) => (
                <label key={permission} className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={groupPermissions.includes(permission)}
                    onChange={() =>
                      setGroupPermissions(togglePermission(groupPermissions, permission))
                    }
                  />
                  {permission}
                </label>
              ))}
            </div>
            <button type="submit" className="inline-button">
              Create group
            </button>
          </form>
        </section>
      ) : null}
    </div>
  );
}
